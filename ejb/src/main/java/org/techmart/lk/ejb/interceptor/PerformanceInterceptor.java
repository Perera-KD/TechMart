package org.techmart.lk.ejb.interceptor;

import jakarta.ejb.EJB;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import org.techmart.lk.ejb.bean.MetricsTrackerBean;

public class PerformanceInterceptor {

    @jakarta.inject.Inject
    @EJB
    private MetricsTrackerBean metricsTracker;

    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception {
        long startTime = System.currentTimeMillis();
        String className = context.getTarget().getClass().getSimpleName();
        String methodName = context.getMethod().getName();

        System.out.println("[PerformanceInterceptor] Entering: " + className + "." + methodName);

        try {
            return context.proceed();
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("[PerformanceInterceptor] Exited: " + className + "." + methodName + " - Duration: " + duration + "ms");

            if (metricsTracker != null) {
                metricsTracker.recordHttpRequest(duration);

                String nameLower = methodName.toLowerCase();
                if (nameLower.contains("find") || nameLower.contains("get") || nameLower.contains("add")
                    || nameLower.contains("update") || nameLower.contains("delete") || nameLower.contains("place")) {
                    metricsTracker.recordDbQuery(duration);
                }
            }
        }
    }
}
