package com.luizfprog.betesportes.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String crestLink;

    @Column
    private String league;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "owner_id", nullable = true)
    private AppUser owner;
}
