package com.luizfprog.betesportes.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity
@Data
@Table(name = "app_user")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String username;

    @Column
    private String password;

    @Column(name = "company_name")
    private String companyName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles;

    public boolean hasRole(String roleName) {
        return roles.contains("ROLE_" + roleName) || roles.contains(roleName);
    }
}
