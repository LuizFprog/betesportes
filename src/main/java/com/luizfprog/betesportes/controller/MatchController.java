package com.luizfprog.betesportes.controller;

import com.luizfprog.betesportes.dto.MatchRequestDTO;
import com.luizfprog.betesportes.entity.Match;
import com.luizfprog.betesportes.repository.MatchRepository;
import com.luizfprog.betesportes.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8081",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class MatchController {
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private TeamRepository teamRepository;

    @PostMapping
    public Match createMatch (@RequestBody MatchRequestDTO dto) {
        Match match = new Match();

        match.setTeamHome(teamRepository.findById(dto.getTeamHomeId())
                .orElseThrow(() -> new RuntimeException("Time da casa não encontrado")));
        match.setTeamGuest(teamRepository.findById(dto.getTeamGuestId())
                .orElseThrow(() -> new RuntimeException("Time visitante não encontrado")));

        match.setStartTime(dto.getStartTime());
        match.setEstimatedEndTime(dto.getEstimatedEndTime());

        return matchRepository.save(match);
    }

    @GetMapping
    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    @DeleteMapping("/{id}")
    public void deleteMatch(@PathVariable Long id) {
        matchRepository.deleteById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Match> updateMatch(@PathVariable Long id, @RequestBody MatchRequestDTO dto) {
        return matchRepository.findById(id)
                .map(existing -> {
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
}
