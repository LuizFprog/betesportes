package com.luizfprog.betesportes.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
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

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "owner_id", nullable = true)
    private AppUser owner;
}
