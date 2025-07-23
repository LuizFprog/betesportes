package com.luizfprog.betesportes.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

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

    @Column
    private String rulesTitle;

    @Column
    private String rulesSubTitle;

    @ElementCollection
    @CollectionTable(name = "offer_rules_paragraphs",
            joinColumns = @JoinColumn(name = "offer_id"))
    @Column(name = "paragraph")
    private List<String> rulesParagraphs;

    @ElementCollection
    @CollectionTable(name = "offer_rules_gifs",
            joinColumns = @JoinColumn(name = "offer_id"))
    @Column(name = "gif_link")
    private List<String> rulesGIFLinks;
}
