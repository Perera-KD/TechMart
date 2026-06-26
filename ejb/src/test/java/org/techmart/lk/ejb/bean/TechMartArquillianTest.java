package org.techmart.lk.ejb.bean;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import jakarta.inject.Inject;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ArquillianExtension.class)
public class TechMartArquillianTest {

    @Inject
    private MetricsTrackerBean metricsTracker;

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class, "techmart-test.jar")
            .addClass(MetricsTrackerBean.class)
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
}
