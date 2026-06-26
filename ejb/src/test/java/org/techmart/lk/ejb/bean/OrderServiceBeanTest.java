package org.techmart.lk.ejb.bean;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.techmart.lk.core.entity.Order;
import org.techmart.lk.core.entity.Product;
import org.techmart.lk.ejb.bean.InventoryCacheBean;
import org.techmart.lk.ejb.bean.MetricsTrackerBean;
import org.techmart.lk.ejb.bean.NotificationServiceBean;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Connection;
import jakarta.jms.Session;
import jakarta.jms.MessageProducer;
import jakarta.jms.MapMessage;
import jakarta.jms.Queue;
import jakarta.jms.Topic;

import java.util.concurrent.Future;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceBeanTest {

    @Mock
    private EntityManager em;

    @Mock
    private InventoryCacheBean inventoryCache;

    @Mock
    private MetricsTrackerBean metricsTracker;

    @Mock
    private NotificationServiceBean notificationService;

    @Mock
    private jakarta.enterprise.concurrent.ManagedExecutorService executorService;

    @Mock
    private ConnectionFactory connectionFactory;

    @Mock
    private Connection connection;

    @Mock
    private Session session;

    @Mock
    private MessageProducer messageProducer;

    @Mock
    private MapMessage mapMessage;

    @Mock
    private Queue orderQueue;

    @Mock
    private Topic auditTopic;

    @InjectMocks
    private OrderServiceBean orderService;

    private Product testProduct;

    @BeforeEach
    public void setUp() throws Exception {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("RTX 5080");
        testProduct.setPrice(120000.0);
        testProduct.setQuantity(10);

        lenient().when(connectionFactory.createConnection()).thenReturn(connection);
        lenient().when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);
        lenient().when(session.createProducer(any())).thenReturn(messageProducer);
        lenient().when(session.createMapMessage()).thenReturn(mapMessage);

        lenient().doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return java.util.concurrent.CompletableFuture.completedFuture(null);
        }).when(executorService).submit(any(Runnable.class));
    }

    @Test
    public void testPlaceOrderSuccess() throws Exception {

        when(em.find(Product.class, 1L)).thenReturn(testProduct);

        when(inventoryCache.getStock(1L)).thenReturn(10);
        when(inventoryCache.decrementStock(1L, 2)).thenReturn(true);

        CompletableFuture<Boolean> future = CompletableFuture.completedFuture(true);
        when(notificationService.sendAsyncNotification(anyString(), anyString())).thenReturn(future);

        Order order = orderService.placeOrder("Kylie", 1L, 2);

        assertNotNull(order);
        assertEquals("Kylie", order.getCustomerName());
        assertEquals(240000.0, order.getTotalAmount());
        assertEquals(8, testProduct.getQuantity());

        verify(em).persist(any(Order.class));
        verify(em).merge(testProduct);
        verify(inventoryCache).decrementStock(1L, 2);
    }

    @Test
    public void testPlaceOrderInsufficientStock() {
        when(em.find(Product.class, 1L)).thenReturn(testProduct);
        when(inventoryCache.getStock(1L)).thenReturn(1);

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            orderService.placeOrder("Kylie", 1L, 2);
        });

        assertTrue(exception.getMessage().contains("Insufficient stock"));
    }

    @Test
    public void testOrderPerformanceBenchmark() throws Exception {

        int iterations = 100;
        long startTime = System.currentTimeMillis();

        when(em.find(Product.class, 1L)).thenReturn(testProduct);
        when(inventoryCache.getStock(1L)).thenReturn(100);
        when(inventoryCache.decrementStock(eq(1L), anyInt())).thenReturn(true);
        CompletableFuture<Boolean> future = CompletableFuture.completedFuture(true);
        lenient().when(notificationService.sendAsyncNotification(anyString(), anyString())).thenReturn(future);

        for (int i = 0; i < iterations; i++) {
            orderService.placeOrder("LoadUser" + i, 1L, 1);
        }

        long totalTime = System.currentTimeMillis() - startTime;
        double avgTime = (double) totalTime / iterations;

        System.out.println("[Benchmark] Processed " + iterations + " orders in " + totalTime + "ms");
        System.out.println("[Benchmark] Average checkout time: " + avgTime + "ms per order");

        assertTrue(avgTime < 100.0, "Average checkout execution time must be under 100ms");
    }
}
