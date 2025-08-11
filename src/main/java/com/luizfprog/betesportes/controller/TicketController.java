package com.luizfprog.betesportes.controller;

import com.luizfprog.betesportes.dto.TicketRequestDTO;
import com.luizfprog.betesportes.dto.VotesSummaryDTO;
import com.luizfprog.betesportes.entity.AppUser;
import com.luizfprog.betesportes.entity.Ticket;
import com.luizfprog.betesportes.repository.AppUserRepository;
import com.luizfprog.betesportes.repository.BetRepository;
import com.luizfprog.betesportes.repository.TicketRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "https://tabela-sports-office.lovable.app",
        "https://promo.apostaganha.bet.br/app",
        "http://localhost:3000"
}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT})
@Tag(name = "Tickets", description = "Gerenciamento de tickets e resumo de votos")
public class TicketController {

    private final TicketRepository ticketRepository;
    private final BetRepository betRepository;
    private final AppUserRepository userRepository;

    @Operation(summary = "Criar ticket", description = "Cria um ticket com as apostas selecionadas.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ticket criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Ticket> createTicket(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do ticket", required = true)
            @RequestBody TicketRequestDTO dto,
            @Parameter(hidden = true) Authentication auth
    ) {
        AppUser user = userRepository
                .findByUsername(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        Ticket ticket = new Ticket();
        ticket.setOwner(user);
        ticket.setMatches(betRepository.findAllById(dto.getMatchIds()));
        ticket.setBetAmount(dto.getBetAmount());
        ticket.setOdd(dto.getOdd());
        ticket.setTicketLink(dto.getTicketLink());
        ticket.setGreenVote(dto.getGreenVote());
        ticket.setRedVote(dto.getRedVote());

        Ticket salvo = ticketRepository.save(ticket);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @Operation(summary = "Listar tickets", description = "Retorna tickets do usuário (ou todos se for ADMIN).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    public List<Ticket> getAllTickets(@Parameter(hidden = true) Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return ticketRepository.findAll();
        }
        AppUser currentUser = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        String company = currentUser.getCompanyName();
        if (company == null) {
            return List.of();
        }
        return ticketRepository.findByOwnerCompanyName(company);
    }

    @Operation(summary = "Tickets futuros", description = "Retorna tickets cujas partidas ainda não iniciaram.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada")
    })
    @GetMapping("/upcoming")
    @SecurityRequirement(name = "bearerAuth")
    public List<Ticket> getUpcomingTickets(@Parameter(hidden = true) Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        LocalDateTime now = LocalDateTime.now();

        if (isAdmin) {
            return ticketRepository.findUpcomingMatchTickets(now);
        }

        AppUser currentUser = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        String company = currentUser.getCompanyName();
        return ticketRepository.findUpcomingMatchTicketsByCompany(now, company);
    }

    @Operation(summary = "Tickets em andamento (single match)", description = "Retorna tickets com exatamente 1 partida em andamento.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada")
    })
    @GetMapping("/ongoing")
    @SecurityRequirement(name = "bearerAuth")
    public List<Ticket> getOngoingSingleMatchTickets(@Parameter(hidden = true) Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        LocalDateTime now = LocalDateTime.now();

        if (isAdmin) {
            return ticketRepository.findOngoingSingleMatchTickets(now);
        }

        AppUser currentUser = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        String company = currentUser.getCompanyName();
        return ticketRepository.findOngoingSingleMatchTicketsByCompany(now, company);
    }

    @Operation(summary = "Tickets finalizados", description = "Retorna tickets cujas partidas já terminaram.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada")
    })
    @GetMapping("/finished")
    @SecurityRequirement(name = "bearerAuth")
    public List<Ticket> getFinishedSingleMatchTickets(@Parameter(hidden = true) Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        LocalDateTime now = LocalDateTime.now();

        if (isAdmin) {
            return ticketRepository.findFinishedMatchTickets(now);
        }

        AppUser currentUser = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        String company = currentUser.getCompanyName();
        return ticketRepository.findFinishedMatchTicketsByCompany(now, company);
    }

    @Operation(summary = "Resumo de votos (hoje)", description = "Retorna soma de greenVote e redVote para tickets com partidas na data atual.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumo retornado")
    })
    @GetMapping("/votes")
    @SecurityRequirement(name = "bearerAuth")
    public VotesSummaryDTO getVotesSummaryForToday(@Parameter(hidden = true) Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        LocalDate today = LocalDate.now();

        if (isAdmin) {
            return ticketRepository.sumVotesForTodayTickets(today);
        }

        AppUser currentUser = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        return ticketRepository.sumVotesForTodayTicketsByCompany(today, currentUser.getCompanyName());
    }

    @Operation(summary = "Atualizar ticket", description = "Atualiza um ticket; apenas ADMIN ou dono da empresa podem atualizar.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket atualizado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Ticket não encontrado")
    })
    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Ticket> updateTicket(
            @Parameter(description = "ID do ticket", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados atualizados do ticket", required = true)
            @RequestBody TicketRequestDTO dto,
            @Parameter(hidden = true) Authentication auth
    ) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return ticketRepository.findById(id)
                .map(existing -> {
                    if (!isAdmin) {
                        AppUser currentUser = userRepository.findByUsername(auth.getName())
                                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
                        String userCompany = currentUser.getCompanyName();
                        String ownerCompany = existing.getOwner().getCompanyName();
                        if (ownerCompany == null || !ownerCompany.equals(userCompany)) {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).<Ticket>build();
                        }
                    }
                    existing.setMatches(betRepository.findAllById(dto.getMatchIds()));
                    existing.setBetAmount(dto.getBetAmount());
                    existing.setOdd(dto.getOdd());
                    existing.setTicketLink(dto.getTicketLink());
                    existing.setGreenVote(dto.getGreenVote());
                    existing.setRedVote(dto.getRedVote());
                    Ticket updated = ticketRepository.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Excluir ticket", description = "Exclui um ticket; apenas ADMIN ou dono da empresa podem excluir.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ticket excluído"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Ticket não encontrado")
    })
    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteTicket(
            @Parameter(description = "ID do ticket", required = true) @PathVariable Long id,
            @Parameter(hidden = true) Authentication auth
    ) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return ticketRepository.findById(id)
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
                    ticketRepository.delete(existing);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
