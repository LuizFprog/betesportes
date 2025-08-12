package com.luizfprog.betesportes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenResponseDTO {
    private String accessToken;
    private long expiresInSeconds; // opcional, para o cliente saber
}
