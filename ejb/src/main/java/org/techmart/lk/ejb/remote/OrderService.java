package org.techmart.lk.ejb.remote;

import jakarta.ejb.Remote;
import java.util.List;
import org.techmart.lk.core.entity.Order;

@Remote
public interface OrderService {
    Order placeOrder(String customerName, Long productId, int quantity) throws Exception;
    List<Order> getAllOrders();
}
