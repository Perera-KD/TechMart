package org.techmart.lk.ejb.bean;

import jakarta.ejb.AsyncResult;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.Stateless;
import java.util.concurrent.Future;

@Stateless
public class NotificationServiceBean {

    @Asynchronous
    public Future<Boolean> sendAsyncNotification(String recipient, String message) {
        long startTime = System.currentTimeMillis();
        System.out.println("[NotificationService] Starting async notification send to: " + recipient);
        
        try {
            // Simulate network latency / SMS gateway delay
            Thread.sleep(1500);
            
            // Check for simulated error condition to show failure recovery
            if (recipient != null && recipient.toLowerCase().contains("error")) {
                throw new RuntimeException("Simulated connection timeout to gateway for: " + recipient);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("[NotificationService] Async notification sent to " + recipient + " in " + duration + "ms");
            return new AsyncResult<>(true);
        } catch (InterruptedException e) {
            System.err.println("[NotificationService] Async task interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
            return new AsyncResult<>(false);
        } catch (Exception e) {
            System.err.println("[NotificationService] Async task encountered error: " + e.getMessage());
            // In a real system, we'd log this, write to a retry queue, or alert administrators.
            // Returning false represents a failed asynchronous run which the caller handles.
            return new AsyncResult<>(false);
        }
    }
}
