package org.techmart.lk.ejb.bean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.ConcurrencyManagement;
import jakarta.ejb.ConcurrencyManagementType;
import jakarta.ejb.DependsOn;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.techmart.lk.core.entity.Product;

@Singleton
@Startup
@DependsOn("DatabaseSeederBean")
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class InventoryCacheBean {

    @PersistenceContext(unitName = "TechMartPU")
    private EntityManager em;

    private final Map<Long, Integer> stockCache = new HashMap<>();
    private long hits = 0;
    private long misses = 0;

    @PostConstruct
    public void init() {
        System.out.println("[InventoryCache] Pre-loading stock levels into cache...");
        refreshCache();
    }

    @Lock(LockType.WRITE)
    public void refreshCache() {
        try {
            List<Product> products = em.createQuery("SELECT p FROM Product p WHERE p.deleted = false", Product.class).getResultList();
            stockCache.clear();
            for (Product p : products) {
                stockCache.put(p.getId(), p.getQuantity());
            }
            System.out.println("[InventoryCache] Loaded " + stockCache.size() + " products into cache.");
        } catch (Exception e) {
            System.err.println("[InventoryCache] Failed to load cache: " + e.getMessage());
        }
    }

    @Lock(LockType.READ)
    public int getStock(Long productId) {
        if (stockCache.containsKey(productId)) {
            hits++;
            return stockCache.get(productId);
        } else {
            misses++;
            // Fallback load from db
            try {
                Product p = em.find(Product.class, productId);
                if (p != null && !p.isDeleted()) {
                    updateStockDirect(productId, p.getQuantity()); // will need a internal call or write lock
                    return p.getQuantity();
                }
            } catch (Exception e) {
                // Ignore
            }
            return 0;
        }
    }

    @Lock(LockType.WRITE)
    private void updateStockDirect(Long productId, int quantity) {
        stockCache.put(productId, quantity);
    }

    @Lock(LockType.WRITE)
    public void updateStock(Long productId, int quantity) {
        stockCache.put(productId, quantity);
    }

    @Lock(LockType.WRITE)
    public boolean decrementStock(Long productId, int quantity) {
        Integer current = stockCache.get(productId);
        if (current == null || current < quantity) {
            return false;
        }
        stockCache.put(productId, current - quantity);
        return true;
    }

    @Lock(LockType.READ)
    public double getCacheHitRate() {
        long total = hits + misses;
        if (total == 0) return 1.0;
        return (double) hits / total;
    }

    @Lock(LockType.READ)
    public long getHits() {
        return hits;
    }

    @Lock(LockType.READ)
    public long getMisses() {
        return misses;
    }
}
