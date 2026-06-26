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

            Thread.sleep(1500);

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

            return new AsyncResult<>(false);
        }
    }
}
