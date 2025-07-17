package com.luizfprog.betesportes.repository;

import com.luizfprog.betesportes.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
