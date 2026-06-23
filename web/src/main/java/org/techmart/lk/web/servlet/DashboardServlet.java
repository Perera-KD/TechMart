package org.techmart.lk.web.servlet;

import jakarta.ejb.EJB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import org.techmart.lk.core.entity.AuditLog;
import org.techmart.lk.ejb.bean.InventoryCacheBean;
import org.techmart.lk.ejb.bean.MetricsTrackerBean;
import org.techmart.lk.ejb.remote.AdminSessionState;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    @EJB
    private MetricsTrackerBean metricsTracker;

    @EJB
    private InventoryCacheBean inventoryCache;

    @EJB
    private org.techmart.lk.ejb.remote.AuditService auditService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession httpSession = req.getSession(false);
        if (httpSession == null || httpSession.getAttribute("username") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Handle metrics reset
        String reset = req.getParameter("reset");
        if ("true".equals(reset)) {
            metricsTracker.reset();
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        // Fetch stateful audit logs from active admin session bean
        AdminSessionState statefulBean = (AdminSessionState) httpSession.getAttribute("adminSession");
        if (statefulBean != null) {
            req.setAttribute("sessionActions", statefulBean.getAuditActions());
        }

        // Fetch DB persistent audit logs (last 15 items)
        List<AuditLog> dbLogs = auditService.getLatestLogs(15);

        // Pass statistics to JSP
        req.setAttribute("metrics", metricsTracker);
        req.setAttribute("cache", inventoryCache);
        req.setAttribute("dbLogs", dbLogs);
        
        req.getRequestDispatcher("/dashboard.jsp").forward(req, resp);
    }
}
