package com.luizfprog.betesportes.dto;

import com.luizfprog.betesportes.entity.AppUser;
import lombok.Data;

import java.util.Set;

@Data
public class UserResponseDTO {
    private String username;
    private Set<String> roles;

    public UserResponseDTO(AppUser user) {
        this.username = user.getUsername();
        this.roles = user.getRoles();
    }
}