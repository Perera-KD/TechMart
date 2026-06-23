package org.techmart.lk.ejb.bean;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.EJB;
import jakarta.ejb.MessageDriven;
import jakarta.jms.MapMessage;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Date;
import org.techmart.lk.core.entity.AuditLog;

@MessageDriven(
    activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Topic"),
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:module/TechMartAuditTopic"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")
    }
)
public class AuditMDB implements MessageListener {

    @PersistenceContext(unitName = "TechMartPU")
    private EntityManager em;

    @EJB
    private MetricsTrackerBean metricsTracker;

    @Override
    public void onMessage(Message message) {
        long startTime = System.currentTimeMillis();
        System.out.println("[AuditMDB] Received message on AuditTopic!");
        try {
            if (message instanceof MapMessage) {
                MapMessage msg = (MapMessage) message;
                String eventType = msg.getString("eventType");
                String logMessage = msg.getString("message");
                long timestamp = msg.getLong("timestamp");

                // Persist the audit log entry inside the DB
                AuditLog log = new AuditLog(eventType, logMessage, new Date(timestamp));
                em.persist(log);
                em.flush(); // Commit write to database

                long duration = System.currentTimeMillis() - startTime;
                metricsTracker.recordJmsMessageProcessed(duration);
                metricsTracker.recordDbQuery(duration); // This is also a database query

                System.out.println("[AuditMDB] Audit event [" + eventType + "] persisted in " + duration + "ms");
            }
        } catch (Exception e) {
            System.err.println("[AuditMDB] Error saving audit log: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
