package com.sellerhub.infrastructure.persistence;

import com.sellerhub.domain.model.Order;
import com.sellerhub.domain.port.OrderRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryOrderRepository implements OrderRepository {
    private final ConcurrentHashMap<String, Order> data = new ConcurrentHashMap<>();

    @Override
    public List<Order> findAll() {
        return data.values().stream().toList();
    }

    @Override
    public List<Order> findBySellerId(String sellerId) {
        return data.values().stream().filter(order -> order.getSellerId().equals(sellerId)).toList();
    }

    @Override
    public Optional<Order> findById(String id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public void replaceAll(List<Order> orders) {
        data.clear();
        orders.forEach(order -> data.put(order.getId(), order));
    }
}
