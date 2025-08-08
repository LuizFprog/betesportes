package com.luizfprog.betesportes.repository;

import com.luizfprog.betesportes.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByOwnerUsername(String username);
}
