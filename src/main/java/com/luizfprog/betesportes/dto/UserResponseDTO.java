package com.luizfprog.betesportes.dto;

import com.luizfprog.betesportes.entity.AppUser;
import lombok.Data;

import java.util.Set;

@Data
public class UserResponseDTO {
    private Long id;
    private String username;
    private String companyName;
    private Set<String> roles;

    public UserResponseDTO(AppUser user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.companyName = user.getCompanyName();
        this.roles = user.getRoles();
    }
}
