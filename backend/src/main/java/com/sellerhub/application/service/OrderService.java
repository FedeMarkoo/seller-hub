package com.sellerhub.application.service;

import com.sellerhub.application.dto.OrderView;
import com.sellerhub.application.dto.PageView;
import com.sellerhub.application.mapper.SellerHubMapper;
import com.sellerhub.application.util.PaginationUtils;
import com.sellerhub.domain.model.QuestionStatus;
import com.sellerhub.domain.model.OrderStatus;
import com.sellerhub.domain.port.OrderRepository;
import com.sellerhub.domain.port.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final QuestionRepository questionRepository;
    private final SellerHubMapper mapper;

    public List<OrderView> findOrders(String sellerId, int requestedLimit) {
        int limit = Math.max(1, Math.min(requestedLimit, 1000));
        var orders = sellerId == null || sellerId.isBlank()
                ? orderRepository.findAll()
                : orderRepository.findBySellerId(sellerId);
        var questions = questionRepository.findAll();
        return orders.stream()
                .map(order -> mapper.toOrderView(order, questions.stream()
                        .filter(q -> q.getOrderId().equals(order.getId()) && q.getStatus() == QuestionStatus.PENDING)
                        .count()))
                .limit(limit)
                .toList();
    }

    public PageView<OrderView> findOrdersPage(String sellerId, int page, int pageSize) {
        return findOrdersPage(sellerId, page, pageSize, "");
    }

    public PageView<OrderView> findOrdersPage(String sellerId, int page, int pageSize, String search) {
        return findOrdersPage(sellerId, page, pageSize, search, null, null, null);
    }

    public PageView<OrderView> findOrdersPage(String sellerId, int page, int pageSize, String search,
                                              OrderStatus status, LocalDate from, LocalDate to) {
        String normalizedSearch = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        List<OrderView> all = findOrders(sellerId, 1000).stream()
                .filter(order -> status == null || order.status() == status)
                .filter(order -> from == null || !order.createdAt().isBefore(from.atStartOfDay().toInstant(ZoneOffset.UTC)))
                .filter(order -> to == null || order.createdAt().isBefore(to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)))
                .filter(order -> matchesSearch(order, normalizedSearch))
                .toList();
        return PaginationUtils.paginate(all, page, pageSize);
    }

    private boolean matchesSearch(OrderView order, String search) {
        if (search.isBlank()) return true;
        String products = order.items().stream()
                .map(item -> item.getProduct())
                .reduce("", (left, right) -> left + " " + right);
        String searchable = (order.id() + " " + order.buyerName() + " " + products).toLowerCase(Locale.ROOT);
        return searchable.contains(search);
    }

    public OrderView findById(String id) {
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Pedido no encontrado: " + id));
        long pending = questionRepository.findAll().stream()
                .filter(q -> q.getOrderId().equals(id) && q.getStatus() == QuestionStatus.PENDING)
                .count();
        return mapper.toOrderView(order, pending);
    }

    public OrderView transition(String id, OrderStatus nextStatus, String sellerId) {
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Pedido no encontrado: " + id));
        if (sellerId != null && !sellerId.equals(order.getSellerId())) {
            throw new com.sellerhub.infrastructure.security.ForbiddenException();
        }
        if (!order.getStatus().canTransitionTo(nextStatus)) {
            throw new IllegalStateException("Transición de estado no permitida: " + order.getStatus() + " -> " + nextStatus);
        }
        order.setStatus(nextStatus);
        orderRepository.save(order);
        return findById(id);
    }
}
