package com.luizfprog.betesportes.repository;

import com.luizfprog.betesportes.dto.VotesSummaryDTO;
import com.luizfprog.betesportes.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    @Query("""
    select distinct t
    from Ticket t
    join t.matches b
    join b.match m
    where m.startTime > :now
    """)
    List<Ticket> findUpcomingMatchTickets(@Param("now") LocalDateTime now);

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
    select distinct t
    from Ticket t
    join t.matches b
    join b.match m
    where m.estimatedEndTime < :now
    """)
    List<Ticket> findFinishedMatchTickets(@Param("now") LocalDateTime now);

    @Query("""
    select new com.luizfprog.betesportes.dto.VotesSummaryDTO(
        coalesce(sum(t.greenVote), 0),
        coalesce(sum(t.redVote), 0)
    )
    from Ticket t
    where exists (
        select 1
        from t.matches b
        join b.match m
        where cast(m.startTime as localdate) = cast(:currentDate as localdate)
    )
    """)
    VotesSummaryDTO sumVotesForTodayTickets(@Param("currentDate") LocalDate currentDate);

    List<Ticket> findByOwnerUsername(String username);

    List<Ticket> findByOwnerCompanyName(String companyName);

    @Query("""
      select new com.luizfprog.betesportes.dto.VotesSummaryDTO(
        coalesce(sum(t.greenVote), 0),
        coalesce(sum(t.redVote), 0)
      )
      from Ticket t
      join t.matches b
      join b.match m
      where cast(m.startTime as localdate) = cast(:currentDate as localdate)
        and t.owner.username = :username
    """)
    VotesSummaryDTO sumVotesForTodayTicketsByOwner(
            @Param("currentDate") LocalDate currentDate,
            @Param("username")    String username
    );

    // upcoming por company
    @Query("""
    select distinct t
    from Ticket t
    join t.matches b
    join b.match m
    where m.startTime > :now
      and t.owner.companyName = :companyName
    """)
    List<Ticket> findUpcomingMatchTicketsByCompany(@Param("now") LocalDateTime now,
                                                   @Param("companyName") String companyName);

    @Query("""
    select t
    from Ticket t
    join t.matches b
    join b.match m
    where m.startTime <= :now and m.estimatedEndTime >= :now
      and t.owner.companyName = :companyName
    group by t
    having count(b) = 1
    """)
    List<Ticket> findOngoingSingleMatchTicketsByCompany(@Param("now") LocalDateTime now,
                                                        @Param("companyName") String companyName);

    @Query("""
    select distinct t
    from Ticket t
    join t.matches b
    join b.match m
    where m.estimatedEndTime < :now
      and t.owner.companyName = :companyName
    """)
    List<Ticket> findFinishedMatchTicketsByCompany(@Param("now") LocalDateTime now,
                                                   @Param("companyName") String companyName);

    // votes summary por company
    @Query("""
      select new com.luizfprog.betesportes.dto.VotesSummaryDTO(
        coalesce(sum(t.greenVote), 0),
        coalesce(sum(t.redVote), 0)
      )
      from Ticket t
      join t.matches b
      join b.match m
      where cast(m.startTime as localdate) = cast(:currentDate as localdate)
        and t.owner.companyName = :companyName
    """)
    VotesSummaryDTO sumVotesForTodayTicketsByCompany(
            @Param("currentDate") LocalDate currentDate,
            @Param("companyName") String companyName
    );

}
