package com.luizfprog.betesportes.repository;

import com.luizfprog.betesportes.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByOwnerUsername(String username);
}
