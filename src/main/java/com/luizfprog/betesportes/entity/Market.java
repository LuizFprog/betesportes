package com.luizfprog.betesportes.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
public class Market {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @ElementCollection
    @CollectionTable(name = "market_choices", joinColumns = @JoinColumn(name = "market_id"))
    @Column(name = "choice")
    private List<String> choices;
}
