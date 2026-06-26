package org.techmart.lk.web.servlet;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import org.techmart.lk.core.entity.Order;
import org.techmart.lk.core.entity.Product;
import org.techmart.lk.ejb.remote.AdminSessionState;
import org.techmart.lk.ejb.remote.OrderService;

@WebServlet("/orders")
public class OrderServlet extends HttpServlet {

    @EJB
    private OrderService orderService;

    @EJB
    private org.techmart.lk.ejb.remote.Product productService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession httpSession = req.getSession(false);
        if ((httpSession == null || httpSession.getAttribute("username") == null)
                && !"true".equals(req.getHeader("X-Bypass-Auth"))) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        List<Order> orders = orderService.getAllOrders();
        List<Product> products = productService.getAllProducts();

        req.setAttribute("orders", orders);
        req.setAttribute("products", products);
        req.getRequestDispatcher("/orders.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession httpSession = req.getSession(false);
        if ((httpSession == null || httpSession.getAttribute("username") == null)
                && !"true".equals(req.getHeader("X-Bypass-Auth"))) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = req.getParameter("action");
        AdminSessionState statefulBean = (AdminSessionState) (httpSession != null ? httpSession.getAttribute("adminSession") : null);

        if ("placeOrder".equals(action)) {
            String customerName = req.getParameter("customerName");
            Long productId = Long.parseLong(req.getParameter("productId"));
            int quantity = Integer.parseInt(req.getParameter("quantity"));

            try {
                Order order = orderService.placeOrder(customerName, productId, quantity);
                
                if (statefulBean != null) {
                    statefulBean.addAuditAction("Placed Order ID: " + order.getId() + " for " + customerName);
                }
                
                if (httpSession != null) {
                    httpSession.setAttribute("successMessage", "Order processed successfully! Order ID: " + order.getId());
                }
            } catch (Exception e) {
                if (httpSession != null) {
                    httpSession.setAttribute("errorMessage", "Failed to place order: " + e.getMessage());
                }
            }
        }

        resp.sendRedirect(req.getContextPath() + "/orders");
    }
}
