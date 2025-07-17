package com.luizfprog.betesportes.repository;

import com.luizfprog.betesportes.entity.Bet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BetRepository extends JpaRepository<Bet, Long> {

}
