package com.luizfprog.betesportes.controller;

import com.luizfprog.betesportes.dto.TeamRequestDTO;
import com.luizfprog.betesportes.entity.AppUser;
import com.luizfprog.betesportes.entity.Team;
import com.luizfprog.betesportes.repository.AppUserRepository;
import com.luizfprog.betesportes.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "https://tabela-sports-office.lovable.app",
        "https://promo.apostaganha.bet.br/app",
        "http://localhost:3000",
        "https://palpites-ag.vercel.app/"
}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@Tag(name = "Teams", description = "Gerenciamento de times")
public class TeamController {

    private final TeamRepository teamRepository;
    private final AppUserRepository userRepository;

    @Operation(summary = "Criar time", description = "Cria um time — requer role ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Time criado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Team> createTeam(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do time", required = true)
            @RequestBody TeamRequestDTO dto,
            @Parameter(hidden = true) Authentication auth
    ) {
        AppUser user = userRepository
                .findByUsername(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        Team team = new Team();
        team.setName(dto.getName());
        team.setCrestLink(dto.getCrestLink());
        team.setLeague(dto.getLeague());
        team.setOwner(user);
        Team saved = teamRepository.save(team);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(summary = "Listar times", description = "Retorna todos os times.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada")
    })
    @GetMapping
    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    @Operation(summary = "Atualizar time", description = "Atualiza time — requer role ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Time atualizado"),
            @ApiResponse(responseCode = "404", description = "Time não encontrado")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Team> updateTeam(
            @Parameter(description = "ID do time", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados atualizados do time", required = true)
            @RequestBody TeamRequestDTO dto,
            @Parameter(hidden = true) Authentication auth
    ) {
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

    @Operation(summary = "Excluir time", description = "Exclui time — requer role ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Time excluído"),
            @ApiResponse(responseCode = "404", description = "Time não encontrado")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteTeam(
            @Parameter(description = "ID do time", required = true) @PathVariable Long id,
            @Parameter(hidden = true) Authentication auth
    ) {
        return teamRepository.findById(id)
                .map(existing -> {
                    teamRepository.delete(existing);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
