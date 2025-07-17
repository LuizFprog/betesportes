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
}
