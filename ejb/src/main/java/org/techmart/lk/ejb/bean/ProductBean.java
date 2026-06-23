package org.techmart.lk.ejb.bean;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.techmart.lk.core.entity.Product;
import org.techmart.lk.core.entity.Warehouse;

@jakarta.interceptor.Interceptors(org.techmart.lk.ejb.interceptor.PerformanceInterceptor.class)
@Stateless
public class ProductBean implements org.techmart.lk.ejb.remote.Product {

    @PersistenceContext(unitName = "TechMartPU")
    private EntityManager em;

    @EJB
    private InventoryCacheBean inventoryCache;

    @Override
    public List<Product> getAllProducts() {
        return em.createQuery("SELECT p FROM Product p WHERE p.deleted = false", Product.class).getResultList();
    }

    @Override
    public Product getProductById(Long id) {
        return em.find(Product.class, id);
    }

    @Override
    public Product addProduct(Product product) {
        em.persist(product);
        em.flush(); // Force database to assign ID
        inventoryCache.updateStock(product.getId(), product.getQuantity());
        return product;
    }

    @Override
    public Product updateProduct(Product product) {
        Product updated = em.merge(product);
        inventoryCache.updateStock(updated.getId(), updated.getQuantity());
        return updated;
    }

    @Override
    public void deleteProduct(Long id) {
        Product p = em.find(Product.class, id);
        if (p != null) {
            p.setDeleted(true);
            em.merge(p);
            // Cache refresh
            inventoryCache.refreshCache();
        }
    }

    @Override
    public List<Warehouse> getAllWarehouses() {
        return em.createQuery("SELECT w FROM Warehouse w", Warehouse.class).getResultList();
    }
}