package com.luizfprog.betesportes.service;

import com.luizfprog.betesportes.entity.AppUser;
import com.luizfprog.betesportes.entity.RefreshToken;
import com.luizfprog.betesportes.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repo;

    // configurar via application.properties: refresh.token.days=30
    private final long refreshTokenDays;

    public RefreshTokenService(RefreshTokenRepository repo,
                               @Value("${refresh.token.days:30}") long refreshTokenDays) {
        this.repo = repo;
        this.refreshTokenDays = refreshTokenDays;
    }

    public RefreshToken createRefreshToken(AppUser user) {
        RefreshToken t = new RefreshToken();
        t.setToken(UUID.randomUUID().toString());
        t.setUser(user);
        t.setExpiryDate(Instant.now().plus(refreshTokenDays, ChronoUnit.DAYS));
        t.setRevoked(false);
        return repo.save(t);
    }

    public RefreshToken findByToken(String token) {
        return repo.findByToken(token).orElse(null);
    }

    public void deleteByUser(AppUser user) {
        repo.deleteAllByUser(user);
    }

    public void revoke(RefreshToken token) {
        token.setRevoked(true);
        repo.save(token);
    }

    public boolean isExpiredOrRevoked(RefreshToken token) {
        return token == null || token.isRevoked() || token.getExpiryDate().isBefore(Instant.now());
    }

    /**
     * Rotates the refresh token: marks old token revoked and returns a newly created refresh token.
     */
    public RefreshToken rotate(RefreshToken old) {
        if (old != null) {
            old.setRevoked(true);
            repo.save(old);
            return createRefreshToken(old.getUser());
        }
        return null;
    }
}
