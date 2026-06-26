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

        jakarta.persistence.EntityManager mockEm = org.mockito.Mockito.mock(jakarta.persistence.EntityManager.class);
        jakarta.persistence.TypedQuery<org.techmart.lk.core.entity.Order> mockQuery =
            org.mockito.Mockito.mock(jakarta.persistence.TypedQuery.class);

        org.mockito.Mockito.when(mockEm.createQuery(org.mockito.Mockito.anyString(), org.mockito.Mockito.eq(org.techmart.lk.core.entity.Order.class)))
            .thenReturn(mockQuery);
        org.mockito.Mockito.when(mockQuery.getResultList())
            .thenReturn(java.util.Collections.emptyList());

        java.lang.reflect.Field emField = OrderServiceBean.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(orderService, mockEm);

        java.util.List<org.techmart.lk.core.entity.Order> orders = orderService.getAllOrders();
        assertNotNull(orders, "Orders list should not be null");
        assertEquals(0, orders.size(), "Mocked orders list should be empty");
    }
}
