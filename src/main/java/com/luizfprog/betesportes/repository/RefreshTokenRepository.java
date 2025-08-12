package com.luizfprog.betesportes.repository;

import com.luizfprog.betesportes.entity.RefreshToken;
import com.luizfprog.betesportes.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteAllByUser(AppUser user);
}
