package com.luizfprog.betesportes.controller;

import com.luizfprog.betesportes.dto.TeamRequestDTO;
import com.luizfprog.betesportes.entity.AppUser;
import com.luizfprog.betesportes.entity.Team;
import com.luizfprog.betesportes.repository.AppUserRepository;
import com.luizfprog.betesportes.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "https://tabela-sports-office.lovable.app",
        "https://promo.apostaganha.bet.br/app",
        "http://localhost:3000"
}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class TeamController {

    private final TeamRepository teamRepository;
    private final AppUserRepository userRepository;

    // 1) CRIAR
    @PostMapping
    public ResponseEntity<Team> createTeam(
            @RequestBody TeamRequestDTO dto,
            Authentication auth
    ) {
        // busca usuário logado
        AppUser user = userRepository
                .findByUsername(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        if (user == null) {
            throw new UsernameNotFoundException("Usuário não encontrado");
        }

        Team team = new Team();
        team.setName(dto.getName());
        team.setCrestLink(dto.getCrestLink());
        team.setLeague(dto.getLeague());
        team.setOwner(user);  // associa dono
        Team saved = teamRepository.save(team);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // 2) LISTAR
    @GetMapping
    public List<Team> getAllTeams(Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return teamRepository.findAll();
        } else {
            return teamRepository.findByOwnerUsername(auth.getName());
        }
    }

    // 3) ATUALIZAR
    @PutMapping("/{id}")
    public ResponseEntity<Team> updateTeam(
            @PathVariable Long id,
            @RequestBody TeamRequestDTO dto,
            Authentication auth
    ) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return teamRepository.findById(id)
                .map(existing -> {
                    // só admin ou dono pode atualizar
                    if (!isAdmin && !existing.getOwner().getUsername().equals(auth.getName())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<Team>build();
                    }

                    existing.setName(dto.getName());
                    existing.setCrestLink(dto.getCrestLink());
                    existing.setLeague(dto.getLeague());
                    Team updated = teamRepository.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 4) DELETAR
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeam(
            @PathVariable Long id,
            Authentication auth
    ) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return teamRepository.findById(id)
                .map(existing -> {
                    // só admin ou dono pode excluir
                    if (!isAdmin && !existing.getOwner().getUsername().equals(auth.getName())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build();
                    }
                    teamRepository.delete(existing);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
