package org.techmart.lk.ejb.bean;

import jakarta.ejb.ConcurrencyManagement;
import jakarta.ejb.ConcurrencyManagementType;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class MetricsTrackerBean {

    private final AtomicLong httpRequestCount = new AtomicLong(0);
    private final AtomicLong totalHttpResponseTime = new AtomicLong(0);

    private final AtomicLong dbQueryCount = new AtomicLong(0);
    private final AtomicLong totalDbQueryTime = new AtomicLong(0);

    private final AtomicLong jmsMessagesSent = new AtomicLong(0);
    private final AtomicLong jmsMessagesProcessed = new AtomicLong(0);
    private final AtomicLong totalJmsProcessingTime = new AtomicLong(0);

    private final AtomicLong jndiLookups = new AtomicLong(0);
    private final AtomicLong totalJndiTime = new AtomicLong(0);

    @Lock(LockType.WRITE)
    public void recordHttpRequest(long durationMs) {
        httpRequestCount.incrementAndGet();
        totalHttpResponseTime.addAndGet(durationMs);
    }

    @Lock(LockType.WRITE)
    public void recordDbQuery(long durationMs) {
        dbQueryCount.incrementAndGet();
        totalDbQueryTime.addAndGet(durationMs);
    }

    @Lock(LockType.WRITE)
    public void recordJmsMessageSent() {
        jmsMessagesSent.incrementAndGet();
    }

    @Lock(LockType.WRITE)
    public void recordJmsMessageProcessed(long durationMs) {
        jmsMessagesProcessed.incrementAndGet();
        totalJmsProcessingTime.addAndGet(durationMs);
    }

    @Lock(LockType.WRITE)
    public void recordJndiLookup(long durationMs) {
        jndiLookups.incrementAndGet();
        totalJndiTime.addAndGet(durationMs);
    }

    @Lock(LockType.READ)
    public long getHttpRequestCount() {
        return httpRequestCount.get();
    }

    @Lock(LockType.READ)
    public double getAverageResponseTime() {
        long count = httpRequestCount.get();
        if (count == 0) return 0.0;
        return (double) totalHttpResponseTime.get() / count;
    }

    @Lock(LockType.READ)
    public long getDbQueryCount() {
        return dbQueryCount.get();
    }

    @Lock(LockType.READ)
    public double getAverageDbQueryTime() {
        long count = dbQueryCount.get();
        if (count == 0) return 0.0;
        return (double) totalDbQueryTime.get() / count;
    }

    @Lock(LockType.READ)
    public long getJmsMessagesSent() {
        return jmsMessagesSent.get();
    }

    @Lock(LockType.READ)
    public long getJmsMessagesProcessed() {
        return jmsMessagesProcessed.get();
    }

    @Lock(LockType.READ)
    public double getAverageJmsProcessingTime() {
        long count = jmsMessagesProcessed.get();
        if (count == 0) return 0.0;
        return (double) totalJmsProcessingTime.get() / count;
    }

    @Lock(LockType.READ)
    public long getJndiLookups() {
        return jndiLookups.get();
    }

    @Lock(LockType.READ)
    public double getAverageJndiTime() {
        long count = jndiLookups.get();
        if (count == 0) return 0.0;
        return (double) totalJndiTime.get() / count;
    }

    @Lock(LockType.WRITE)
    public void reset() {
        httpRequestCount.set(0);
        totalHttpResponseTime.set(0);
        dbQueryCount.set(0);
        totalDbQueryTime.set(0);
        jmsMessagesSent.set(0);
        jmsMessagesProcessed.set(0);
        totalJmsProcessingTime.set(0);
        jndiLookups.set(0);
        totalJndiTime.set(0);
    }
}
