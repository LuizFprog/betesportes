package com.luizfprog.betesportes.controller;

import com.luizfprog.betesportes.dto.OfferRequestDTO;
import com.luizfprog.betesportes.entity.AppUser;
import com.luizfprog.betesportes.entity.Offer;
import com.luizfprog.betesportes.repository.AppUserRepository;
import com.luizfprog.betesportes.repository.OfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/offers")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "https://tabela-sports-office.lovable.app",
        "https://promo.apostaganha.bet.br/app",
        "http://localhost:3000"
}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class OfferController {

    private final OfferRepository offerRepository;
    private final AppUserRepository userRepository;

    // --- CREATE ---
    @PostMapping
    public ResponseEntity<Offer> createOffer(
            @RequestBody OfferRequestDTO dto,
            Authentication auth
    ) {
        AppUser user = userRepository
                .findByUsername(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        if (user == null) {
            throw new UsernameNotFoundException("Usuário não encontrado");
        }

        Offer offer = new Offer();
        offer.setName(dto.getName());
        offer.setOfferDescription(dto.getOfferDescription());
        offer.setOfferImageLink(dto.getOfferImageLink());
        offer.setOfferButtonLink(dto.getOfferButtonLink());
        offer.setRulesTitle(dto.getRulesTitle());
        offer.setRulesSubTitle(dto.getRulesSubTitle());
        offer.setRulesParagraphs(dto.getRulesParagraphs());
        offer.setRulesGIFLinks(dto.getRulesGIFLinks());
        offer.setOwner(user);

        Offer saved = offerRepository.save(offer);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // --- READ ---
    @GetMapping
    public List<Offer> getAllOffers(Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return offerRepository.findAll();
        }
        return offerRepository.findByOwnerUsername(auth.getName());
    }

    // --- UPDATE ---
    @PutMapping("/{id}")
    public ResponseEntity<Offer> updateOffer(
            @PathVariable Long id,
            @RequestBody OfferRequestDTO dto,
            Authentication auth
    ) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return offerRepository.findById(id)
                .map(existing -> {
                    if (!isAdmin && !existing.getOwner().getUsername().equals(auth.getName())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<Offer>build();
                    }

                    existing.setName(dto.getName());
                    existing.setOfferDescription(dto.getOfferDescription());
                    existing.setOfferImageLink(dto.getOfferImageLink());
                    existing.setOfferButtonLink(dto.getOfferButtonLink());
                    existing.setRulesTitle(dto.getRulesTitle());
                    existing.setRulesSubTitle(dto.getRulesSubTitle());
                    existing.setRulesParagraphs(dto.getRulesParagraphs());
                    existing.setRulesGIFLinks(dto.getRulesGIFLinks());

                    Offer updated = offerRepository.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // --- DELETE ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffer(
            @PathVariable Long id,
            Authentication auth
    ) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return offerRepository.findById(id)
                .map(existing -> {
                    if (!isAdmin && !existing.getOwner().getUsername().equals(auth.getName())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build();
                    }
                    offerRepository.delete(existing);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
