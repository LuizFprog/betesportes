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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;

@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "https://tabela-sports-office.lovable.app",
        "https://promo.apostaganha.bet.br/app",
        "http://localhost:3000",
        "https://palpites-ag.vercel.app/"
}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@Tag(name = "Matches", description = "Gerenciamento de partidas")
public class MatchController {

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final AppUserRepository userRepository;

    @Operation(summary = "Criar partida", description = "Cria uma partida associada ao usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Partida criada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Match> createMatch(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados da partida", required = true)
            @RequestBody MatchRequestDTO dto,
            @Parameter(hidden = true) Authentication auth
    ) {
        AppUser user = userRepository
                .findByUsername(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
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

    @Operation(summary = "Listar partidas", description = "Retorna partidas do usuário (ou todas se for ADMIN).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    public List<Match> getAllMatches(@Parameter(hidden = true) Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return matchRepository.findAll();
        }
        AppUser currentUser = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        String company = currentUser.getCompanyName();
        if (company == null) {
            return List.of();
        }
        return matchRepository.findByOwnerCompanyName(company);
    }

    @Operation(summary = "Atualizar partida", description = "Atualiza partida — apenas ADMIN ou dono da empresa podem atualizar.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Partida atualizada"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Partida não encontrada")
    })
    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Match> updateMatch(
            @Parameter(description = "ID da partida", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados atualizados da partida", required = true)
            @RequestBody MatchRequestDTO dto,
            @Parameter(hidden = true) Authentication auth
    ) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return matchRepository.findById(id)
                .map(existing -> {
                    if (!isAdmin) {
                        AppUser currentUser = userRepository.findByUsername(auth.getName())
                                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
                        String userCompany = currentUser.getCompanyName();
                        String ownerCompany = existing.getOwner().getCompanyName();
                        if (ownerCompany == null || !ownerCompany.equals(userCompany)) {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).<Match>build();
                        }
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

    @Operation(summary = "Excluir partida", description = "Exclui uma partida — apenas ADMIN ou dono da empresa podem excluir.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Partida excluída"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Partida não encontrada")
    })
    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteMatch(
            @Parameter(description = "ID da partida", required = true) @PathVariable Long id,
            @Parameter(hidden = true) Authentication auth
    ) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return matchRepository.findById(id)
                .map(existing -> {
                    if (!isAdmin) {
                        AppUser currentUser = userRepository.findByUsername(auth.getName())
                                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
                        String userCompany = currentUser.getCompanyName();
                        String ownerCompany = existing.getOwner().getCompanyName();
                        if (ownerCompany == null || !ownerCompany.equals(userCompany)) {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build();
                        }
                    }
                    matchRepository.delete(existing);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
