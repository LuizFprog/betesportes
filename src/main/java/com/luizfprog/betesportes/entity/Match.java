package com.luizfprog.betesportes.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "team_home_id")
    private Team teamHome;

    @ManyToOne
    @JoinColumn(name = "team_guest_id")
    private Team teamGuest;

    @Column
    private LocalDateTime startTime;

    @Column
    private LocalDateTime estimatedEndTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "owner_id", nullable = true)
    private AppUser owner;
}
