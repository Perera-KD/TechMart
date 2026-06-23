package org.techmart.lk.ejb.bean;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.MapMessage;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.Topic;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.techmart.lk.core.entity.Order;
import org.techmart.lk.core.entity.OrderItem;
import org.techmart.lk.core.entity.Product;
import org.techmart.lk.ejb.remote.OrderService;

@jakarta.interceptor.Interceptors(org.techmart.lk.ejb.interceptor.PerformanceInterceptor.class)
@Stateless
public class OrderServiceBean implements OrderService {

    @PersistenceContext(unitName = "TechMartPU")
    private EntityManager em;

    @EJB
    private InventoryCacheBean inventoryCache;

    @EJB
    private MetricsTrackerBean metricsTracker;

    @EJB
    private NotificationServiceBean notificationService;

    // Inject JMS ConnectionFactory and Destination Resources using JNDI lookups
    @Resource(lookup = "java:module/TechMartConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Resource(lookup = "java:module/TechMartOrderQueue")
    private Queue orderQueue;

    @Resource(lookup = "java:module/TechMartAuditTopic")
    private Topic auditTopic;

    @Resource
    private jakarta.enterprise.concurrent.ManagedExecutorService executorService;

    @Override
    public Order placeOrder(String customerName, Long productId, int quantity) throws Exception {
        System.out.println("[OrderServiceBean] Processing checkout transaction for customer: " + customerName);
        
        long startTime = System.currentTimeMillis();
        
        // 1. Validate Product and Check Stock via Cache (Sub-second optimization check)
        Product product = em.find(Product.class, productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found with ID: " + productId);
        }

        int cachedStock = inventoryCache.getStock(productId);
        if (cachedStock < quantity) {
            throw new IllegalStateException("Insufficient stock! Available: " + cachedStock + ", Requested: " + quantity);
        }

        // 2. Perform Stock Reservation and DB update
        boolean cacheDeducted = inventoryCache.decrementStock(productId, quantity);
        if (!cacheDeducted) {
            throw new IllegalStateException("Race condition detected! Stock cache update failed.");
        }

        product.setQuantity(product.getQuantity() - quantity);
        em.merge(product);

        // 3. Create and persist Order & OrderItem
        double total = product.getPrice() * quantity;
        Order order = new Order(customerName, new Date(), "PENDING", total);
        em.persist(order);
        
        OrderItem item = new OrderItem(order, product, quantity, product.getPrice());
        em.persist(item);
        
        // Flush database to assign IDs
        em.flush();
        
        long dbDuration = System.currentTimeMillis() - startTime;
        metricsTracker.recordDbQuery(dbDuration);

        System.out.println("[OrderServiceBean] Order ID: " + order.getId() + " persisted in " + dbDuration + "ms");

        // 4. Send Message to Order Queue (JMS Point-to-Point)
        sendOrderJmsMessage(order, product, quantity);

        // 5. Send Message to Audit Topic (JMS Publish/Subscribe)
        sendAuditJmsMessage("ORDER_CREATED", "Order #" + order.getId() + " placed by " + customerName + " for total Rs." + total);

        // 6. Demonstrate @Asynchronous method call with Future handling
        // We trigger the async notification, which completes in the background.
        Future<Boolean> asyncResult = notificationService.sendAsyncNotification(
            customerName + "@techmart.lk", 
            "Order #" + order.getId() + " of Rs." + total + " has been successfully submitted!"
        );
        
        // In a real controller we might wait for it or just let it finish. 
        // We can check it asynchronously later or schedule a timeout check.
        // Use container-managed executor service for specification compliance
        executorService.submit(() -> {
            try {
                // Wait up to 3 seconds for async notification status to check for errors (Failure recovery test)
                Boolean sent = asyncResult.get(3, TimeUnit.SECONDS);
                System.out.println("[OrderServiceBean] Async notification delivery success: " + sent);
            } catch (Exception e) {
                System.err.println("[OrderServiceBean] Async notification delivery failed (Circuit/Timeout recovery): " + e.getMessage());
            }
        });

        return order;
    }

    @Override
    public List<Order> getAllOrders() {
        return em.createQuery("SELECT o FROM Order o ORDER BY o.orderDate DESC", Order.class).getResultList();
    }

    private void sendOrderJmsMessage(Order order, Product product, int qty) {
        try (Connection conn = connectionFactory.createConnection();
             Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
             
            MessageProducer producer = session.createProducer(orderQueue);
            MapMessage msg = session.createMapMessage();
            msg.setLong("orderId", order.getId());
            msg.setString("customerName", order.getCustomerName());
            msg.setDouble("totalAmount", order.getTotalAmount());
            msg.setString("productName", product.getName());
            msg.setInt("quantity", qty);
            msg.setLong("timestamp", System.currentTimeMillis());
            
            producer.send(msg);
            metricsTracker.recordJmsMessageSent();
            System.out.println("[OrderServiceBean] Sent order JMS message for Order #" + order.getId());
            
        } catch (Exception e) {
            System.err.println("[OrderServiceBean] Failed to send order JMS message: " + e.getMessage());
        }
    }

    private void sendAuditJmsMessage(String eventType, String logMessage) {
        try (Connection conn = connectionFactory.createConnection();
             Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
             
            MessageProducer producer = session.createProducer(auditTopic);
            MapMessage msg = session.createMapMessage();
            msg.setString("eventType", eventType);
            msg.setString("message", logMessage);
            msg.setLong("timestamp", System.currentTimeMillis());
            
            producer.send(msg);
            metricsTracker.recordJmsMessageSent();
            System.out.println("[OrderServiceBean] Sent audit JMS message for event: " + eventType);
            
        } catch (Exception e) {
            System.err.println("[OrderServiceBean] Failed to send audit JMS message: " + e.getMessage());
        }
    }
}
