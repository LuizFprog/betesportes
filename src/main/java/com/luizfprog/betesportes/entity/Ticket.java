package com.luizfprog.betesportes.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    @JoinTable(
            name = "ticket_bet",
            joinColumns = @JoinColumn(name = "ticket_id"),
            inverseJoinColumns = @JoinColumn(name = "bet_id")
    )
    private List<Bet> matches;

    @Column
    private Double betAmount;

    @Column
    private Double odd;

    @Column
    private String ticketLink;

    @Column
    private Integer greenVote;

    @Column
    private Integer redVote;
}
