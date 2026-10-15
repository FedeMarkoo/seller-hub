package com.sellerhub;

import com.sellerhub.domain.model.OrderStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderStatusTest {
    @Test
    void allowsOnlyForwardOperationalTransitions() {
        assertTrue(OrderStatus.PROCESSING.canTransitionTo(OrderStatus.SHIPPED));
        assertTrue(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.DELIVERED));
        assertTrue(OrderStatus.PROCESSING.canTransitionTo(OrderStatus.CANCELLED));
        assertFalse(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.PROCESSING));
        assertFalse(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.SHIPPED));
    }
}
