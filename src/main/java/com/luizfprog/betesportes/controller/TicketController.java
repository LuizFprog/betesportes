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
public class TicketController {

    private final TicketRepository ticketRepository;
    private final BetRepository betRepository;
    private final AppUserRepository userRepository;

    // --- CRIAR ---
    @PostMapping
    public ResponseEntity<Ticket> createTicket(
            @RequestBody TicketRequestDTO dto,
            Authentication auth
    ) {
        AppUser user = userRepository
                .findByUsername(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        if (user == null) {
            throw new UsernameNotFoundException("Usuário não encontrado");
        }

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

    // --- LISTAR ---
    @GetMapping
    public List<Ticket> getAllTickets(Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return ticketRepository.findAll();
        }
        return ticketRepository.findByOwnerUsername(auth.getName());
    }

    @GetMapping("/upcoming")
    public List<Ticket> getUpcomingTickets(Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        List<Ticket> list = ticketRepository.findUpcomingMatchTickets(LocalDateTime.now());
        if (isAdmin) {
            return list;
        }
        return list.stream()
                .filter(t -> t.getOwner().getUsername().equals(auth.getName()))
                .toList();
    }

    @GetMapping("/ongoing")
    public List<Ticket> getOngoingSingleMatchTickets(Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        List<Ticket> list = ticketRepository.findOngoingSingleMatchTickets(LocalDateTime.now());
        if (isAdmin) {
            return list;
        }
        return list.stream()
                .filter(t -> t.getOwner().getUsername().equals(auth.getName()))
                .toList();
    }

    @GetMapping("/finished")
    public List<Ticket> getFinishedSingleMatchTickets(Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        List<Ticket> list = ticketRepository.findFinishedMatchTickets(LocalDateTime.now());
        if (isAdmin) {
            return list;
        }
        return list.stream()
                .filter(t -> t.getOwner().getUsername().equals(auth.getName()))
                .toList();
    }

    // --- VOTOS ---
    @GetMapping("/votes")
    public VotesSummaryDTO getVotesSummaryForToday(Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        LocalDate today = LocalDate.now();

        if (isAdmin) {
            return ticketRepository.sumVotesForTodayTickets(today);
        }
        return ticketRepository.sumVotesForTodayTicketsByOwner(today, auth.getName());
    }

    // --- ATUALIZAR ---
    @PutMapping("/{id}")
    public ResponseEntity<Ticket> updateTicket(
            @PathVariable Long id,
            @RequestBody TicketRequestDTO dto,
            Authentication auth
    ) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return ticketRepository.findById(id)
                .map(existing -> {
                    // só admin ou dono pode atualizar
                    if (!isAdmin && !existing.getOwner().getUsername().equals(auth.getName())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<Ticket>build();
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

    // --- DELETAR ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(
            @PathVariable Long id,
            Authentication auth
    ) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return ticketRepository.findById(id)
                .map(existing -> {
                    // só admin ou dono pode excluir
                    if (!isAdmin && !existing.getOwner().getUsername().equals(auth.getName())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build();
                    }
                    ticketRepository.delete(existing);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
