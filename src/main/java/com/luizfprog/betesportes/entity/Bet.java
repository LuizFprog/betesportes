package com.luizfprog.betesportes.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Bet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;

    @Column
    private String betType;

    @Column
    private String betChoice;

    @Column
    private String betDescription;

    @Column(nullable = true)
    private Boolean earlyPayment;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "owner_id", nullable = true)
    private AppUser owner;
}
