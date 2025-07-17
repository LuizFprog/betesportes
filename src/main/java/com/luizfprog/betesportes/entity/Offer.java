package com.luizfprog.betesportes.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String offerDescription;

    @Column
    private String offerImageLink;

    @Column
    private String offerButtonLink;
}
