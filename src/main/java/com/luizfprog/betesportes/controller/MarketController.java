package com.luizfprog.betesportes.controller;

import com.luizfprog.betesportes.dto.MarketRequestDTO;
import com.luizfprog.betesportes.entity.AppUser;
import com.luizfprog.betesportes.entity.Market;
import com.luizfprog.betesportes.repository.AppUserRepository;
import com.luizfprog.betesportes.repository.MarketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/markets")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "https://tabela-sports-office.lovable.app",
        "https://promo.apostaganha.bet.br/app",
        "http://localhost:3000"
}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class MarketController {

    private final MarketRepository marketRepository;
    private final AppUserRepository userRepository;

    // --- CREATE ---
    @PostMapping
    public ResponseEntity<Market> createMarket(
            @RequestBody MarketRequestDTO dto,
            Authentication auth
    ) {
        AppUser user = userRepository
                .findByUsername(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        if (user == null) {
            throw new UsernameNotFoundException("Usuário não encontrado");
        }
        Market market = new Market();
        market.setName(dto.getName());
        market.setChoices(dto.getChoices());
        market.setOwner(user);
        Market saved = marketRepository.save(market);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // --- READ ---
    @GetMapping
    public List<Market> getAllMarkets(Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return marketRepository.findAll();
        }
        return marketRepository.findByOwnerUsername(auth.getName());
    }

    // --- UPDATE ---
    @PutMapping("/{id}")
    public ResponseEntity<Market> updateMarket(
            @PathVariable Long id,
            @RequestBody MarketRequestDTO dto,
            Authentication auth
    ) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return marketRepository.findById(id)
                .map(existing -> {
                    if (!isAdmin && !existing.getOwner().getUsername().equals(auth.getName())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<Market>build();
                    }
                    existing.setName(dto.getName());
                    existing.setChoices(dto.getChoices());
                    Market updated = marketRepository.save(existing);
                    return ResponseEntity.ok(updated);
                }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // --- DELETE ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMarket(
            @PathVariable Long id,
            Authentication auth
    ) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return marketRepository.findById(id)
                .map(existing -> {
                    if (!isAdmin && !existing.getOwner().getUsername().equals(auth.getName())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build();
                    }
                    marketRepository.delete(existing);
                    return ResponseEntity.noContent().<Void>build();
                }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
