package com.sellerhub.infrastructure.security;

public record DemoIdentity(DemoRole role, String sellerId) {
    public boolean canAccessSeller(String requestedSellerId) {
        return sellerId == null || sellerId.equals(requestedSellerId);
    }
}
