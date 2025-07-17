package com.luizfprog.betesportes.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MatchRequestDTO {
    private Long teamHomeId;
    private Long teamGuestId;
    private LocalDateTime startTime;
    private LocalDateTime estimatedEndTime;
}
