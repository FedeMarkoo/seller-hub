package com.sellerhub.infrastructure.security;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Duration;
import java.time.Instant;

@Service
public class DemoSessionService {
    private static final Set<String> SELLERS = Set.of("seller-01", "seller-02", "seller-03");
    private final SecureRandom random = new SecureRandom();
    private final Map<String, SessionEntry> sessions = new ConcurrentHashMap<>();
    private final Duration sessionTtl = Duration.ofHours(8);

    public String create(DemoRole role, String sellerId) {
        if (role == DemoRole.SELLER && (sellerId == null || sellerId.isBlank())) {
            throw new IllegalArgumentException("La sesión de vendedor requiere un vendedor");
        }
        if (sellerId != null && !SELLERS.contains(sellerId)) {
            throw new IllegalArgumentException("Vendedor demo inválido");
        }
        byte[] token = new byte[32];
        random.nextBytes(token);
        String session = Base64.getUrlEncoder().withoutPadding().encodeToString(token);
        sessions.put(session, new SessionEntry(new DemoIdentity(role, sellerId), Instant.now().plus(sessionTtl)));
        return session;
    }

    public DemoIdentity require(String token) {
        if (token == null || token.isBlank()) throw new UnauthorizedException();
        var entry = Optional.ofNullable(sessions.get(token)).orElseThrow(UnauthorizedException::new);
        if (entry.expiresAt().isBefore(Instant.now())) {
            sessions.remove(token);
            throw new UnauthorizedException();
        }
        return entry.identity();
    }

    public void revoke(String token) { if (token != null) sessions.remove(token); }

    public void requireOperations(String token) {
        if (require(token).role() != DemoRole.OPERATIONS) throw new ForbiddenException();
    }

    public void requireSeller(String token, String requestedSellerId) {
        if (!require(token).canAccessSeller(requestedSellerId)) throw new ForbiddenException();
    }

    private record SessionEntry(DemoIdentity identity, Instant expiresAt) {}
}
