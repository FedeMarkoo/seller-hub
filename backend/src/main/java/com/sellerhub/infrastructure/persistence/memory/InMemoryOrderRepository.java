package com.sellerhub.infrastructure.persistence.memory;

import com.sellerhub.domain.model.Order;
import com.sellerhub.domain.port.OrderRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryOrderRepository implements OrderRepository {
    private final ConcurrentHashMap<String, Order> orders = new ConcurrentHashMap<>();

    @Override public List<Order> findAll() { return new ArrayList<>(orders.values()); }
    @Override public List<Order> findBySellerId(String sellerId) {
        return orders.values().stream().filter(order -> sellerId.equals(order.getSellerId())).toList();
    }
    @Override public Optional<Order> findById(String id) { return Optional.ofNullable(orders.get(id)); }
    @Override public void save(Order order) { orders.put(order.getId(), order); }
    @Override public synchronized void replaceAll(List<Order> values) {
        orders.clear();
        values.forEach(order -> orders.put(order.getId(), order));
    }
}
