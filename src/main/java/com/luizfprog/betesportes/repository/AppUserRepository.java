package com.luizfprog.betesportes.repository;

import com.luizfprog.betesportes.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String name);

    boolean existsByUsername(String username);

    List<AppUser> findByCompanyName(String companyName);
}
