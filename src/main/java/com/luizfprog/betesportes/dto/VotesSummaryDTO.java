package com.luizfprog.betesportes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VotesSummaryDTO {
    private Long totalGreenVotes;
    private Long totalRedVotes;
}
