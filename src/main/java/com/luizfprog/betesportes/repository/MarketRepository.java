package com.luizfprog.betesportes.repository;

import com.luizfprog.betesportes.entity.Market;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MarketRepository extends JpaRepository<Market, Long> {
    List<Market> findByOwnerUsername(String username);
}
