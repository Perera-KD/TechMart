package org.techmart.lk.ejb.remote;

import jakarta.ejb.Remote;
import java.util.List;
import org.techmart.lk.core.entity.Warehouse;

@Remote
public interface Product {
    List<org.techmart.lk.core.entity.Product> getAllProducts();
    org.techmart.lk.core.entity.Product getProductById(Long id);
    org.techmart.lk.core.entity.Product addProduct(org.techmart.lk.core.entity.Product product);
    org.techmart.lk.core.entity.Product updateProduct(org.techmart.lk.core.entity.Product product);
    void deleteProduct(Long id);
    List<Warehouse> getAllWarehouses();
}
