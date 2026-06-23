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
import org.techmart.lk.core.entity.Product;
import org.techmart.lk.core.entity.Warehouse;
import org.techmart.lk.ejb.remote.AdminSessionState;

@WebServlet("/products")
public class ProductServlet extends HttpServlet {

    @EJB
    private org.techmart.lk.ejb.remote.Product productService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession httpSession = req.getSession(false);
        if (httpSession == null || httpSession.getAttribute("username") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // List products & warehouses
        List<Product> products = productService.getAllProducts();
        List<Warehouse> warehouses = productService.getAllWarehouses();

        req.setAttribute("products", products);
        req.setAttribute("warehouses", warehouses);
        req.getRequestDispatcher("/products.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession httpSession = req.getSession(false);
        if (httpSession == null || httpSession.getAttribute("username") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = req.getParameter("action");
        AdminSessionState statefulBean = (AdminSessionState) httpSession.getAttribute("adminSession");

        try {
            if ("add".equals(action)) {
                String name = req.getParameter("name");
                String desc = req.getParameter("description");
                double price = Double.parseDouble(req.getParameter("price"));
                int qty = Integer.parseInt(req.getParameter("quantity"));
                Long warehouseId = Long.parseLong(req.getParameter("warehouseId"));

                Product product = new Product(name, desc, price, qty, warehouseId);
                productService.addProduct(product);
                
                if (statefulBean != null) {
                    statefulBean.addAuditAction("Added product: " + name);
                }
            } else if ("update".equals(action)) {
                Long id = Long.parseLong(req.getParameter("id"));
                String name = req.getParameter("name");
                String desc = req.getParameter("description");
                double price = Double.parseDouble(req.getParameter("price"));
                int qty = Integer.parseInt(req.getParameter("quantity"));
                Long warehouseId = Long.parseLong(req.getParameter("warehouseId"));

                Product product = productService.getProductById(id);
                if (product != null) {
                    product.setName(name);
                    product.setDescription(desc);
                    product.setPrice(price);
                    product.setQuantity(qty);
                    product.setWarehouseId(warehouseId);
                    productService.updateProduct(product);
                    
                    if (statefulBean != null) {
                        statefulBean.addAuditAction("Updated product ID: " + id);
                    }
                }
            } else if ("delete".equals(action)) {
                Long id = Long.parseLong(req.getParameter("id"));
                productService.deleteProduct(id);
                
                if (statefulBean != null) {
                    statefulBean.addAuditAction("Deleted product ID: " + id);
                }
            }
        } catch (Exception e) {
            req.setAttribute("error", "Error performing action: " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/products");
    }
}
