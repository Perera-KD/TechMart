package org.techmart.lk.web.filter;

import jakarta.ejb.EJB;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.techmart.lk.ejb.bean.MetricsTrackerBean;

@WebFilter("/*")
public class PerformanceFilter implements Filter {

    @EJB
    private MetricsTrackerBean metricsTracker;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        long startTime = System.nanoTime();
        
        try {
            chain.doFilter(request, response);
        } finally {
            long durationNs = System.nanoTime() - startTime;
            long durationMs = durationNs / 1_000_000;
            
            // Exclude static assets if needed, record standard servlet/jsp traffic
            if (request instanceof HttpServletRequest) {
                HttpServletRequest req = (HttpServletRequest) request;
                String uri = req.getRequestURI();
                if (!uri.contains("/css/") && !uri.contains("/js/") && !uri.contains("/images/")) {
                    if (metricsTracker != null) {
                        metricsTracker.recordHttpRequest(durationMs);
                    }
                }
            }
        }
    }

    @Override
    public void destroy() {}
}
