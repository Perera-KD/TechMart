package org.techmart.lk.ejb.bean;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import jakarta.inject.Inject;
import org.techmart.lk.ejb.remote.OrderService;
import org.techmart.lk.ejb.interceptor.PerformanceInterceptor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ArquillianExtension.class)
public class TechMartArquillianTest {

    @Inject
    private MetricsTrackerBean metricsTracker;

    @Inject
    private OrderServiceBean orderService;

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class, "techmart-test.jar")
            .addClass(MetricsTrackerBean.class)
            .addClass(OrderServiceBean.class)
            .addClass(OrderService.class)
            .addClass(PerformanceInterceptor.class)
            .addAsManifestResource("META-INF/persistence.xml", "persistence.xml")
            .addAsManifestResource(new StringAsset(
                "<beans xmlns=\"https://jakarta.ee/xml/ns/jakartaee\" " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "xsi:schemaLocation=\"https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/beans_4_0.xsd\" " +
                "bean-discovery-mode=\"all\"></beans>"), "beans.xml");
    }

    @Test
    public void testMetricsTrackerInjection() {
        assertNotNull(metricsTracker, "MetricsTrackerBean should be injected");
    }

    @Test
    public void testMetricsTrackerRecording() {
        assertNotNull(metricsTracker);
        metricsTracker.reset();
        metricsTracker.recordHttpRequest(120);
        assertEquals(1, metricsTracker.getHttpRequestCount());
        assertEquals(120.0, metricsTracker.getAverageResponseTime());
    }

    @Test
    public void testOrderServiceBeanCall() throws Exception {
        assertNotNull(orderService, "OrderServiceBean should be injected");
        assertNotNull(metricsTracker, "MetricsTrackerBean should be injected");
        
        java.util.Map<String, String> properties = new java.util.HashMap<>();
        properties.put("jakarta.persistence.schema-generation.database.action", "drop-and-create");
        properties.put("eclipselink.ddl-generation", "drop-and-create-tables");
        properties.put("eclipselink.ddl-generation.output-mode", "database");

        jakarta.persistence.EntityManagerFactory emf = 
            jakarta.persistence.Persistence.createEntityManagerFactory("TechMartTestPU", properties);
        jakarta.persistence.EntityManager em = emf.createEntityManager();
        
        java.lang.reflect.Field emField = OrderServiceBean.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(orderService, em);

        metricsTracker.reset();
        long initialCount = metricsTracker.getHttpRequestCount();

        java.util.List<org.techmart.lk.core.entity.Order> orders = orderService.getAllOrders();
        assertNotNull(orders, "Orders list should not be null");
        assertEquals(0, orders.size(), "Orders database should be empty initially");

        assertEquals(initialCount + 1, metricsTracker.getHttpRequestCount(),
            "PerformanceInterceptor should fire and increment the HTTP request count");

        em.close();
        emf.close();
    }
}
