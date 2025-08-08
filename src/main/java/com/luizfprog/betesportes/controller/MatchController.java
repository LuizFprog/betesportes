package com.luizfprog.betesportes.controller;

import com.luizfprog.betesportes.dto.MatchRequestDTO;
import com.luizfprog.betesportes.entity.AppUser;
import com.luizfprog.betesportes.entity.Match;
import com.luizfprog.betesportes.repository.AppUserRepository;
import com.luizfprog.betesportes.repository.MatchRepository;
import com.luizfprog.betesportes.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "https://tabela-sports-office.lovable.app",
        "https://promo.apostaganha.bet.br/app",
        "http://localhost:3000"
}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class MatchController {

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final AppUserRepository userRepository;

    // --- CREATE ---
    @PostMapping
    public ResponseEntity<Match> createMatch(
            @RequestBody MatchRequestDTO dto,
            Authentication auth
    ) {
        AppUser user = userRepository
                .findByUsername(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        if (user == null) {
            throw new UsernameNotFoundException("Usuário não encontrado");
        }

        Match match = new Match();
        match.setOwner(user);
        match.setTeamHome(teamRepository.findById(dto.getTeamHomeId())
                .orElseThrow(() -> new RuntimeException("Time da casa não encontrado")));
        match.setTeamGuest(teamRepository.findById(dto.getTeamGuestId())
                .orElseThrow(() -> new RuntimeException("Time visitante não encontrado")));
        match.setStartTime(dto.getStartTime());
        match.setEstimatedEndTime(dto.getEstimatedEndTime());

        Match saved = matchRepository.save(match);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // --- READ ---
    @GetMapping
    public List<Match> getAllMatches(Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return matchRepository.findAll();
        }
        return matchRepository.findByOwnerUsername(auth.getName());
    }

    // --- UPDATE ---
    @PutMapping("/{id}")
    public ResponseEntity<Match> updateMatch(
            @PathVariable Long id,
            @RequestBody MatchRequestDTO dto,
            Authentication auth
    ) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return matchRepository.findById(id)
                .map(existing -> {
                    // só admin ou dono pode atualizar
                    if (!isAdmin && !existing.getOwner().getUsername().equals(auth.getName())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<Match>build();
                    }

                    existing.setTeamHome(teamRepository.findById(dto.getTeamHomeId())
                            .orElseThrow(() -> new RuntimeException("Time da casa não encontrado")));
                    existing.setTeamGuest(teamRepository.findById(dto.getTeamGuestId())
                            .orElseThrow(() -> new RuntimeException("Time visitante não encontrado")));
                    existing.setStartTime(dto.getStartTime());
                    existing.setEstimatedEndTime(dto.getEstimatedEndTime());

                    Match updated = matchRepository.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // --- DELETE ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMatch(
            @PathVariable Long id,
            Authentication auth
    ) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return matchRepository.findById(id)
                .map(existing -> {
                    // só admin ou dono pode excluir
                    if (!isAdmin && !existing.getOwner().getUsername().equals(auth.getName())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build();
                    }
                    matchRepository.delete(existing);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
