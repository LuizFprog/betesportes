package com.luizfprog.betesportes.controller;

import com.luizfprog.betesportes.dto.BetRequestDTO;
import com.luizfprog.betesportes.entity.AppUser;
import com.luizfprog.betesportes.entity.Bet;
import com.luizfprog.betesportes.repository.AppUserRepository;
import com.luizfprog.betesportes.repository.BetRepository;
import com.luizfprog.betesportes.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bets")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "https://tabela-sports-office.lovable.app",
        "https://promo.apostaganha.bet.br/app",
        "http://localhost:3000"
}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class BetController {

    private final BetRepository betRepository;
    private final MatchRepository matchRepository;
    private final AppUserRepository userRepository;

    // --- CREATE ---
    @PostMapping
    public ResponseEntity<Bet> createBet(
            @RequestBody BetRequestDTO dto,
            Authentication auth
    ) {
        AppUser user = userRepository
                .findByUsername(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        if (user == null) {
            throw new UsernameNotFoundException("Usuário não encontrado");
        }
        Bet bet = new Bet();
        bet.setOwner(user);
        bet.setMatch(matchRepository.findById(dto.getMatchId())
                .orElseThrow(() -> new RuntimeException("Partida não encontrada")));
        bet.setBetType(dto.getBetType());
        bet.setBetChoice(dto.getBetChoice());
        bet.setBetDescription(dto.getBetDescription());
        bet.setEarlyPayment(dto.getEarlyPayment());
        Bet saved = betRepository.save(bet);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // --- READ ---
    @GetMapping
    public List<Bet> getAllBets(Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return betRepository.findAll();
        }
        return betRepository.findByOwnerUsername(auth.getName());
    }

    // --- UPDATE ---
    @PutMapping("/{id}")
    public ResponseEntity<Bet> updateBet(
            @PathVariable Long id,
            @RequestBody BetRequestDTO dto,
            Authentication auth
    ) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return betRepository.findById(id)
                .map(existing -> {
                    if (!isAdmin && !existing.getOwner().getUsername().equals(auth.getName())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<Bet>build();
                    }
                    existing.setMatch(matchRepository.findById(dto.getMatchId())
                            .orElseThrow(() -> new RuntimeException("Partida não encontrada")));
                    existing.setBetType(dto.getBetType());
                    existing.setBetChoice(dto.getBetChoice());
                    existing.setBetDescription(dto.getBetDescription());
                    existing.setEarlyPayment(dto.getEarlyPayment());
                    Bet updated = betRepository.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // --- DELETE ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBet(
            @PathVariable Long id,
            Authentication auth
    ) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return betRepository.findById(id)
                .map(existing -> {
                    if (!isAdmin && !existing.getOwner().getUsername().equals(auth.getName())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build();
                    }
                    betRepository.delete(existing);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
