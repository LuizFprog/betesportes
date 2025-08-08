package com.luizfprog.betesportes.repository;

import com.luizfprog.betesportes.entity.Bet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BetRepository extends JpaRepository<Bet, Long> {
    List<Bet> findByOwnerUsername(String username);
}
