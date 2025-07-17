package com.luizfprog.betesportes.dto;

import lombok.Data;

import java.util.List;

@Data
public class MarketRequestDTO {
    private String name;
    private List<String> choices;
}
