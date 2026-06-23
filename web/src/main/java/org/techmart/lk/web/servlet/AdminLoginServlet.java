package org.techmart.lk.web.servlet;

import jakarta.ejb.EJB;
import javax.naming.NamingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.techmart.lk.ejb.remote.AdminSessionState;
import org.techmart.lk.web.util.ServiceLocator;

@WebServlet("/login")
public class AdminLoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if ("logout".equals(action)) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                AdminSessionState adminState = (AdminSessionState) session.getAttribute("adminSession");
                if (adminState != null) {
                    try {
                        adminState.logout(); // Removes EJB state
                    } catch (Exception e) {
                        // Ignore
                    }
                }
                session.invalidate();
            }
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        // Fulfill Admin Authentication Requirement
        if ("admin".equals(username) && "admin123".equals(password)) {
            HttpSession httpSession = req.getSession(true);
            
            // Fulfill EJB State Management: Look up stateful bean programmatically
            try {
                // Programmatic lookup to demonstrate JNDI performance monitoring
                AdminSessionState adminState = ServiceLocator.lookup(
                    "java:global/techmart/ejb-module/AdminSessionStateBean!org.techmart.lk.ejb.remote.AdminSessionState", 
                    AdminSessionState.class
                );
                
                adminState.login(username);
                adminState.addAuditAction("User logged in via web portal");
                
                // Store the stateful bean reference in HTTP session
                httpSession.setAttribute("adminSession", adminState);
                httpSession.setAttribute("username", username);
                
                resp.sendRedirect(req.getContextPath() + "/products");
                
            } catch (NamingException e) {
                System.err.println("[AdminLoginServlet] JNDI Lookup failed for stateful bean: " + e.getMessage());
                req.setAttribute("error", "JNDI EJB Authentication context failure: " + e.getMessage());
                req.getRequestDispatcher("/login.jsp").forward(req, resp);
            }
        } else {
            req.setAttribute("error", "Invalid username or password!");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
    }
}
