package com.luizfprog.betesportes.dto;

import lombok.Data;

import java.util.List;

@Data
public class OfferRequestDTO {
    private String name;
    private String offerDescription;
    private String offerImageLink;
    private String offerButtonLink;

    private String rulesTitle;
    private String rulesSubTitle;
    private List<String> rulesParagraphs;
    private List<String> rulesGIFLinks;
}
