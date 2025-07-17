package com.luizfprog.betesportes.repository;

import com.luizfprog.betesportes.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
}
