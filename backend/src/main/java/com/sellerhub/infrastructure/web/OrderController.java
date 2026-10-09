package com.sellerhub.infrastructure.web;

import com.sellerhub.application.dto.OrderView;
import com.sellerhub.application.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public List<OrderView> find(@RequestParam(required = false) String sellerId) {
        return orderService.findOrders(sellerId);
    }

    @GetMapping("/{id}")
    public OrderView findById(@PathVariable String id) {
        return orderService.findById(id);
    }
}
