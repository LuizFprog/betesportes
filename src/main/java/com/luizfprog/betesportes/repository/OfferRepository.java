package com.luizfprog.betesportes.repository;

import com.luizfprog.betesportes.entity.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer> findByOwnerUsername(String username);

    List<Offer> findByOwnerCompanyName(String companyName);
}
