package com.luizfprog.betesportes.repository;

import com.luizfprog.betesportes.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match, Long> {
}
