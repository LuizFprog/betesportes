package com.luizfprog.betesportes.dto;

import lombok.Data;

import java.util.List;

@Data
public class TicketRequestDTO {
    private List<Long> matchIds;
    private Double betAmount;
    private Double odd;
    private String ticketLink;
    private Integer greenVote;
    private Integer redVote;
}
