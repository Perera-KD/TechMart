package org.techmart.lk.ejb.bean;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.EJB;
import jakarta.ejb.MessageDriven;
import jakarta.jms.MapMessage;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;

@MessageDriven(
    activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:module/TechMartOrderQueue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")
    }
)
public class OrderNotificationMDB implements MessageListener {

    @EJB
    private MetricsTrackerBean metricsTracker;

    @Override
    public void onMessage(Message message) {
        long startTime = System.currentTimeMillis();
        System.out.println("[OrderNotificationMDB] Received message on OrderQueue!");
        try {
            if (message instanceof MapMessage) {
                MapMessage msg = (MapMessage) message;
                Long orderId = msg.getLong("orderId");
                String customerName = msg.getString("customerName");
                double totalAmount = msg.getDouble("totalAmount");
                String productName = msg.getString("productName");
                int quantity = msg.getInt("quantity");

                Thread.sleep(800);

                long duration = System.currentTimeMillis() - startTime;
                metricsTracker.recordJmsMessageProcessed(duration);

                System.out.println("[OrderNotificationMDB] Async processing complete for Order #" + orderId
                    + " (" + quantity + "x " + productName + ") total Rs. " + totalAmount
                    + " in " + duration + "ms");
            }
        } catch (Exception e) {
            System.err.println("[OrderNotificationMDB] Error processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
