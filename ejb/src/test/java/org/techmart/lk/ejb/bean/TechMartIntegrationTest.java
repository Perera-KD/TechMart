package org.techmart.lk.ejb.bean;

import jakarta.jms.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;
import org.techmart.lk.core.entity.AuditLog;
import org.techmart.lk.core.entity.Order;
import org.techmart.lk.core.entity.Product;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TechMartIntegrationTest {

    private EntityManagerFactory emf;
    private EntityManager em;

    private InMemoryJmsProvider jmsProvider;
    private OrderServiceBean orderService;
    private InventoryCacheBean inventoryCache;
    private MetricsTrackerBean metricsTracker;
    private NotificationServiceBean notificationService;
    private ProductBean productBean;

    private OrderNotificationMDB orderMdb;
    private AuditMDB auditMdb;

    @BeforeEach
    public void setup() throws Exception {

        createTablesManually();

        emf = Persistence.createEntityManagerFactory("TechMartTestPU");
        em = emf.createEntityManager();
        jmsProvider = new InMemoryJmsProvider();

        orderService = new OrderServiceBean();
        inventoryCache = new InventoryCacheBean();
        metricsTracker = new MetricsTrackerBean();
        notificationService = new NotificationServiceBean();
        productBean = new ProductBean();

        orderMdb = new OrderNotificationMDB();
        auditMdb = new AuditMDB();

        injectField(orderService, "em", em);
        injectField(orderService, "inventoryCache", inventoryCache);
        injectField(orderService, "metricsTracker", metricsTracker);
        injectField(orderService, "notificationService", notificationService);

        injectField(productBean, "em", em);
        injectField(productBean, "inventoryCache", inventoryCache);

        injectField(inventoryCache, "em", em);

        injectField(orderMdb, "metricsTracker", metricsTracker);

        injectField(auditMdb, "em", em);
        injectField(auditMdb, "metricsTracker", metricsTracker);

        injectField(orderService, "connectionFactory", jmsProvider.getConnectionFactory());
        injectField(orderService, "orderQueue", jmsProvider.getQueue("java:module/TechMartOrderQueue"));
        injectField(orderService, "auditTopic", jmsProvider.getTopic("java:module/TechMartAuditTopic"));

        jakarta.enterprise.concurrent.ManagedExecutorService directExecutor =
            (jakarta.enterprise.concurrent.ManagedExecutorService) java.lang.reflect.Proxy.newProxyInstance(
                TechMartIntegrationTest.class.getClassLoader(),
                new Class<?>[]{jakarta.enterprise.concurrent.ManagedExecutorService.class},
                new java.lang.reflect.InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
                        if ("submit".equals(method.getName()) && args.length == 1 && args[0] instanceof Runnable) {
                            ((Runnable) args[0]).run();
                            return java.util.concurrent.CompletableFuture.completedFuture(null);
                        }
                        if ("execute".equals(method.getName()) && args.length == 1 && args[0] instanceof Runnable) {
                            ((Runnable) args[0]).run();
                            return null;
                        }
                        return null;
                    }
                }
            );
        injectField(orderService, "executorService", directExecutor);

        jmsProvider.registerQueueListener("java:module/TechMartOrderQueue", orderMdb);
        jmsProvider.registerTopicListener("java:module/TechMartAuditTopic", msg -> {
            em.getTransaction().begin();
            try {
                auditMdb.onMessage(msg);
                em.getTransaction().commit();
            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new RuntimeException(e);
            }
        });

        em.getTransaction().begin();
        Product testProduct = new Product();
        testProduct.setName("RTX 5080");
        testProduct.setPrice(1500.0);
        testProduct.setQuantity(10);
        em.persist(testProduct);
        em.getTransaction().commit();

        inventoryCache.init();
        metricsTracker.reset();
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (em != null) {
            em.close();
        }
        if (emf != null) {
            emf.close();
        }
        jmsProvider.clearListeners();

        dropTablesManually();
    }

    @Test
    public void testOrderCheckoutSuccessFlow() throws Exception {
        System.out.println("--- Starting Integration Test: Success Flow ---");

        Product product = em.createQuery("SELECT p FROM Product p WHERE p.name = 'RTX 5080'", Product.class).getSingleResult();
        Long productId = product.getId();

        assertEquals(10, inventoryCache.getStock(productId));
        assertEquals(0, metricsTracker.getJmsMessagesSent());

        em.getTransaction().begin();

        Order order = orderService.placeOrder("Alice", productId, 3);
        assertNotNull(order);
        assertEquals("Alice", order.getCustomerName());
        assertEquals(4500.0, order.getTotalAmount());

        em.getTransaction().commit();

        jmsProvider.commitTx();

        Thread.sleep(1200);

        em.clear();
        Product updatedProduct = em.find(Product.class, productId);
        assertEquals(7, updatedProduct.getQuantity(), "Database product quantity should be decremented");
        assertEquals(7, inventoryCache.getStock(productId), "Inventory cache should be updated");

        List<Order> orders = em.createQuery("SELECT o FROM Order o", Order.class).getResultList();
        assertEquals(1, orders.size(), "Order should be saved in database");
        assertEquals("Alice", orders.get(0).getCustomerName());

        List<AuditLog> auditLogs = em.createQuery("SELECT a FROM AuditLog a", AuditLog.class).getResultList();
        assertEquals(1, auditLogs.size(), "AuditMDB should have persisted an AuditLog");
        assertTrue(auditLogs.get(0).getMessage().contains("Order"), "Audit message should detail the order");
        assertEquals("ORDER_CREATED", auditLogs.get(0).getEventType());

        assertTrue(metricsTracker.getJmsMessagesSent() >= 2, "Should record sending Queue and Topic messages");
        assertTrue(metricsTracker.getJmsMessagesProcessed() >= 2, "Should record processing queue and topic messages");
    }

    @Test
    public void testOrderCheckoutRollbackFlow() throws Exception {
        System.out.println("--- Starting Integration Test: Rollback Flow ---");

        Product product = em.createQuery("SELECT p FROM Product p WHERE p.name = 'RTX 5080'", Product.class).getSingleResult();
        Long productId = product.getId();

        assertEquals(10, inventoryCache.getStock(productId));

        em.getTransaction().begin();

        assertThrows(java.lang.IllegalStateException.class, () -> {
            orderService.placeOrder("Bob", productId, 15);
        });

        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        jmsProvider.rollbackTx();

        Thread.sleep(500);

        em.clear();
        Product rolledBackProduct = em.find(Product.class, productId);
        assertEquals(10, rolledBackProduct.getQuantity(), "Database product quantity should remain unchanged");
        assertEquals(10, inventoryCache.getStock(productId), "Inventory cache should remain unchanged");

        List<Order> orders = em.createQuery("SELECT o FROM Order o", Order.class).getResultList();
        assertEquals(0, orders.size(), "No order should be saved in the database");

        List<AuditLog> auditLogs = em.createQuery("SELECT a FROM AuditLog a", AuditLog.class).getResultList();
        assertEquals(0, auditLogs.size(), "No audit log should be saved (MDB was not triggered)");
    }

    private void createTablesManually() throws Exception {
        Class.forName("org.h2.Driver");
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:techmart_db;DB_CLOSE_DELAY=-1;MODE=LEGACY", "sa", "");
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "description VARCHAR(255), " +
                "price DOUBLE NOT NULL, " +
                "quantity INT NOT NULL, " +
                "warehouse_id BIGINT, " +
                "version INT DEFAULT 0, " +
                "deleted BOOLEAN DEFAULT FALSE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS orders (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "customer_name VARCHAR(100) NOT NULL, " +
                "order_date TIMESTAMP NOT NULL, " +
                "status VARCHAR(20) NOT NULL, " +
                "total_amount DOUBLE NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS order_items (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "order_id BIGINT NOT NULL, " +
                "product_id BIGINT NOT NULL, " +
                "quantity INT NOT NULL, " +
                "unit_price DOUBLE NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS audit_logs (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "event_type VARCHAR(50) NOT NULL, " +
                "message VARCHAR(500) NOT NULL, " +
                "timestamp TIMESTAMP NOT NULL)");
        }
    }

    private void dropTablesManually() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:techmart_db;DB_CLOSE_DELAY=-1;MODE=LEGACY", "sa", "");
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS audit_logs");
            stmt.execute("DROP TABLE IF EXISTS order_items");
            stmt.execute("DROP TABLE IF EXISTS orders");
            stmt.execute("DROP TABLE IF EXISTS products");
        }
    }

    @Test
    public void testProductSoftDeleteFlow() throws Exception {
        System.out.println("--- Starting Integration Test: Product Soft Delete Flow ---");

        Product product = em.createQuery("SELECT p FROM Product p WHERE p.name = 'RTX 5080'", Product.class).getSingleResult();
        Long productId = product.getId();

        List<Product> activeProductsBefore = productBean.getAllProducts();
        assertTrue(activeProductsBefore.stream().anyMatch(p -> p.getId().equals(productId)), "Product should be present in active products list");
        assertEquals(10, inventoryCache.getStock(productId), "Inventory cache should track active product");

        em.getTransaction().begin();
        productBean.deleteProduct(productId);
        em.getTransaction().commit();

        List<Product> activeProductsAfter = productBean.getAllProducts();
        assertFalse(activeProductsAfter.stream().anyMatch(p -> p.getId().equals(productId)), "Soft-deleted product must be filtered out of active lists");
        assertEquals(0, inventoryCache.getStock(productId), "Inventory cache should return 0 stock for soft-deleted product");

        em.clear();
        Product rawDbRecord = em.find(Product.class, productId);
        assertNotNull(rawDbRecord, "Database record must NOT be physically removed (soft delete audit safety)");
        assertTrue(rawDbRecord.isDeleted(), "Database record deleted flag must be true");
    }

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
