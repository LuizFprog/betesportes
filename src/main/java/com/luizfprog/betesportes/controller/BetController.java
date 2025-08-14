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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;

@RestController
@RequestMapping("/bets")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "https://tabela-sports-office.lovable.app",
        "https://promo.apostaganha.bet.br/app",
        "http://localhost:3000",
        "https://palpites-ag.vercel.app/"
}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@Tag(name = "Bets", description = "CRUD e buscas de apostas")
public class BetController {

    private final BetRepository betRepository;
    private final MatchRepository matchRepository;
    private final AppUserRepository userRepository;

    @Operation(summary = "Criar aposta", description = "Cria uma aposta associada ao usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Aposta criada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Bet> createBet(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados da aposta", required = true)
            @RequestBody BetRequestDTO dto,
            @Parameter(hidden = true) Authentication auth
    ) {
        AppUser user = userRepository
                .findByUsername(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
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

    @Operation(summary = "Listar apostas", description = "Retorna apostas do usuário (ou todas se for ADMIN).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    public List<Bet> getAllBets(@Parameter(hidden = true) Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return betRepository.findAll();
        }
        AppUser currentUser = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        String company = currentUser.getCompanyName();
        if (company == null) {
            return List.of();
        }
        return betRepository.findByOwnerCompanyName(company);
    }

    @Operation(summary = "Atualizar aposta", description = "Atualiza aposta; apenas ADMIN ou dono da empresa podem atualizar.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aposta atualizada"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Aposta não encontrada")
    })
    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Bet> updateBet(
            @Parameter(description = "ID da aposta", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados atualizados da aposta", required = true)
            @RequestBody BetRequestDTO dto,
            @Parameter(hidden = true) Authentication auth
    ) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return betRepository.findById(id)
                .map(existing -> {
                    if (!isAdmin) {
                        AppUser currentUser = userRepository.findByUsername(auth.getName())
                                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
                        String userCompany = currentUser.getCompanyName();
                        String ownerCompany = existing.getOwner().getCompanyName();
                        if (ownerCompany == null || !ownerCompany.equals(userCompany)) {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).<Bet>build();
                        }
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

    @Operation(summary = "Excluir aposta", description = "Exclui uma aposta; apenas ADMIN ou dono da empresa podem excluir.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Aposta excluída"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Aposta não encontrada")
    })
    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteBet(
            @Parameter(description = "ID da aposta", required = true) @PathVariable Long id,
            @Parameter(hidden = true) Authentication auth
    ) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return betRepository.findById(id)
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
                    betRepository.delete(existing);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
