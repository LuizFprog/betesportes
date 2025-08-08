package com.luizfprog.betesportes.dto;

import lombok.Data;

import java.util.Set;

@Data
public class RegistrationRequestDTO {
    private String username;
    private String password;
    private Set<String> roles;
}