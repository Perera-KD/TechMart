package org.techmart.lk.ejb.bean;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.techmart.lk.core.entity.Product;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryCacheBeanTest {

    @Mock
    private EntityManager em;

    @InjectMocks
    private InventoryCacheBean inventoryCache;

    private Product testProduct;

    @BeforeEach
    public void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("RTX 5080");
        testProduct.setPrice(120000.0);
        testProduct.setQuantity(10);
    }

    @Test
    public void testCacheHitAndMissHandling() {
        // Cache initially empty. Querying product 1 should trigger database fallback
        when(em.find(Product.class, 1L)).thenReturn(testProduct);

        int stock = inventoryCache.getStock(1L);
        assertEquals(10, stock);
        assertEquals(1, inventoryCache.getMisses()); // Miss count incremented
        assertEquals(0, inventoryCache.getHits());

        // Second lookup should retrieve from cache (Hit)
        stock = inventoryCache.getStock(1L);
        assertEquals(10, stock);
        assertEquals(1, inventoryCache.getMisses());
        assertEquals(1, inventoryCache.getHits());   // Hit count incremented
        assertEquals(0.5, inventoryCache.getCacheHitRate()); // 1 hit, 1 miss = 50% hit rate
    }

    @Test
    public void testDecrementStockSuccess() {
        inventoryCache.updateStock(1L, 10);

        boolean success = inventoryCache.decrementStock(1L, 3);
        assertTrue(success);
        assertEquals(7, inventoryCache.getStock(1L));
    }

    @Test
    public void testDecrementStockFailure() {
        inventoryCache.updateStock(1L, 5);

        boolean success = inventoryCache.decrementStock(1L, 8); // asking for more than exists
        assertFalse(success);
        assertEquals(5, inventoryCache.getStock(1L)); // unchanged
    }
}
