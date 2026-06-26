package org.techmart.lk.web.util;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import org.techmart.lk.ejb.bean.MetricsTrackerBean;

public class ServiceLocator {

    private static final Map<String, Object> cache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> T lookup(String jndiName, Class<T> clazz) throws NamingException {
        // Stateful session beans must not be cached because they require a new instance per client
        boolean cacheable = !jndiName.contains("AdminSessionStateBean");
        
        if (cacheable && cache.containsKey(jndiName)) {
            return (T) cache.get(jndiName);
        }

        long startTime = System.nanoTime();
        InitialContext ctx = new InitialContext();
        Object resolved = ctx.lookup(jndiName);
        
        long durationNs = System.nanoTime() - startTime;
        long durationMs = durationNs / 1_000_000;
        
        System.out.println("[ServiceLocator] JNDI Lookup for: " + jndiName + " took " + durationMs + "ms");
        
        try {
            MetricsTrackerBean tracker = (MetricsTrackerBean) ctx.lookup("java:global/techmart/ejb-module/MetricsTrackerBean!org.techmart.lk.ejb.bean.MetricsTrackerBean");
            if (tracker != null) {
                tracker.recordJndiLookup(durationMs);
            }
        } catch (Exception e) {
            // Ignore if metrics tracker is not available or has different JNDI name during startup
        }

        if (cacheable) {
            cache.put(jndiName, resolved);
        }
        return (T) resolved;
    }
}
