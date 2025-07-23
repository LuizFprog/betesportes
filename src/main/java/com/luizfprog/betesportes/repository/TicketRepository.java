package com.luizfprog.betesportes.repository;

import com.luizfprog.betesportes.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    @Query("""
    select t
    from Ticket t
    join t.matches b
    join b.match m
    where m.startTime > :now
    group by t
    having count(b) = 1
    """)
    List<Ticket> findUpcomingSingleMatchTickets(@Param("now") LocalDateTime now);

    @Query("""
    select t
    from Ticket t
    join t.matches b
    join b.match m
    where m.startTime <= :now and m.estimatedEndTime >= :now
    group by t
    having count(b) = 1
    """)
    List<Ticket> findOngoingSingleMatchTickets(@Param("now") LocalDateTime now);

    @Query("""
    select t
    from Ticket t
    join t.matches b
    join b.match m
    where m.estimatedEndTime < :now
    group by t
    having count(b) = 1
    """)
    List<Ticket> findFinishedSingleMatchTickets(@Param("now") LocalDateTime now);


}
