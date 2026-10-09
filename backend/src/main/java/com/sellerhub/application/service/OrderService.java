package com.sellerhub.application.service;

import com.sellerhub.application.dto.OrderView;
import com.sellerhub.domain.model.QuestionStatus;
import com.sellerhub.domain.port.OrderRepository;
import com.sellerhub.domain.port.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final QuestionRepository questionRepository;

    public List<OrderView> findOrders(String sellerId) {
        var orders = sellerId == null || sellerId.isBlank()
                ? orderRepository.findAll()
                : orderRepository.findBySellerId(sellerId);
        var questions = questionRepository.findAll();
        return orders.stream().map(order -> OrderView.from(order, questions.stream()
                .filter(q -> q.getOrderId().equals(order.getId()) && q.getStatus() == QuestionStatus.PENDING)
                .count())).toList();
    }

    public OrderView findById(String id) {
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Pedido no encontrado: " + id));
        long pending = questionRepository.findAll().stream()
                .filter(q -> q.getOrderId().equals(id) && q.getStatus() == QuestionStatus.PENDING).count();
        return OrderView.from(order, pending);
    }
}
