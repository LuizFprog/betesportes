package com.luizfprog.betesportes.dto;

import lombok.Data;

@Data
public class BetRequestDTO {
    private Long matchId;
    private String betType;
    private String betChoice;
    private String betDescription;
}
