package com.luizfprog.betesportes.controller;

import com.luizfprog.betesportes.dto.BetRequestDTO;
import com.luizfprog.betesportes.entity.Bet;
import com.luizfprog.betesportes.repository.BetRepository;
import com.luizfprog.betesportes.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bets")
@RequiredArgsConstructor
public class BetController {
    @Autowired
    private BetRepository betRepository;
    @Autowired
    private MatchRepository matchRepository;

    @PostMapping
    public Bet createBet(@RequestBody BetRequestDTO dto) {
        Bet bet = new Bet();
        bet.setMatch(matchRepository.findById(dto.getMatchId())
                .orElseThrow(() -> new RuntimeException("Partida não encontrada")));
        bet.setBetType(dto.getBetType());
        bet.setBetChoice(dto.getBetChoice());
        bet.setBetDescription(dto.getBetDescription());
        bet.setEarlyPayment(dto.getEarlyPayment());
        return betRepository.save(bet);
    }

    @GetMapping
    public List<Bet> getAllBets() { return betRepository.findAll(); }

    @PutMapping("/{id}")
    public ResponseEntity<Bet> updateBet (@PathVariable Long id, @RequestBody BetRequestDTO dto) {
        return betRepository.findById(id)
                .map(existing -> {
                    existing.setMatch(matchRepository.findById(dto.getMatchId())
                            .orElseThrow(() -> new RuntimeException("Partida não encontrada")));
                    existing.setBetType(dto.getBetType());
                    existing.setBetChoice(dto.getBetChoice());
                    existing.setBetDescription(dto.getBetDescription());
                    existing.setEarlyPayment(dto.getEarlyPayment());

                    Bet updated = betRepository.save(existing);
                    return ResponseEntity.ok(updated);
                }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public void deleteBet (@PathVariable Long id) {
        betRepository.deleteById(id);
    }
}
