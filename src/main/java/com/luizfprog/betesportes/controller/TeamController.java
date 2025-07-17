package com.luizfprog.betesportes.controller;

import com.luizfprog.betesportes.dto.TeamRequestDTO;
import com.luizfprog.betesportes.entity.Team;
import com.luizfprog.betesportes.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
@CrossOrigin(origins = "tabela-sports-office.lovable.app",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class TeamController {
    @Autowired
    private TeamRepository teamRepository;

    @PostMapping
    public Team createTeam(@RequestBody TeamRequestDTO dto)
    {
        Team team = new Team();
        team.setName(dto.getName());
        team.setCrestLink(dto.getCrestLink());
        team.setLeague(dto.getLeague());
        return teamRepository.save(team);
    }

    @GetMapping
    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    @DeleteMapping("/{id}")
    public void deleteTeam (@PathVariable Long id) {
        teamRepository.deleteById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Team> updateTeam (@PathVariable Long id, @RequestBody TeamRequestDTO dto) {
        return teamRepository.findById(id)
                .map(existing -> {
                    existing.setName(dto.getName());
                    existing.setCrestLink(dto.getCrestLink());
                    existing.setLeague(dto.getLeague());

                    Team updated = teamRepository.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
