package com.sellerhub.domain.port;

import com.sellerhub.domain.model.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    List<Order> findAll();
    List<Order> findBySellerId(String sellerId);
    Optional<Order> findById(String id);
    void replaceAll(List<Order> orders);
}
