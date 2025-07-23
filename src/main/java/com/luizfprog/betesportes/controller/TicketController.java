package com.luizfprog.betesportes.controller;

import com.luizfprog.betesportes.dto.TicketRequestDTO;
import com.luizfprog.betesportes.entity.Ticket;
import com.luizfprog.betesportes.repository.BetRepository;
import com.luizfprog.betesportes.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://tabela-sports-office.lovable.app",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT})
public class TicketController {
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private BetRepository betRepository;

    @PostMapping
    public Ticket createTicket (@RequestBody TicketRequestDTO dto) {
        Ticket ticket = new Ticket();
        ticket.setMatches(betRepository.findAllById(dto.getMatchIds()));
        ticket.setBetAmount(dto.getBetAmount());
        ticket.setOdd(dto.getOdd());
        ticket.setTicketLink(dto.getTicketLink());
        ticket.setGreenVote(dto.getGreenVote());
        ticket.setRedVote(dto.getRedVote());
        return ticketRepository.save(ticket);
    }

    @GetMapping
    public List<Ticket> getAllTickets() { return ticketRepository.findAll(); }

    @GetMapping("/upcoming")
    public List<Ticket> getUpcomingSingleMatchTickets() {
        return ticketRepository.findUpcomingSingleMatchTickets(LocalDateTime.now());
    }

    @GetMapping("/ongoing")
    public List<Ticket> getOngoingSingleMatchTickets() {
        return ticketRepository.findOngoingSingleMatchTickets(LocalDateTime.now());
    }

    @GetMapping("/finished")
    public List<Ticket> getFinishedSingleMatchTickets() {
        return ticketRepository.findFinishedSingleMatchTickets(LocalDateTime.now());
    }

    @DeleteMapping("/{id}")
    public void deleteTicket(@PathVariable Long id) { ticketRepository.deleteById(id); }

    @PutMapping("/{id}")
    public ResponseEntity<Ticket> updateTicket (@PathVariable Long id, @RequestBody TicketRequestDTO dto) {
        return ticketRepository.findById(id)
                .map(existing -> {
                    existing.setMatches(betRepository.findAllById(dto.getMatchIds()));
                    existing.setBetAmount(dto.getBetAmount());
                    existing.setOdd(dto.getOdd());
                    existing.setTicketLink(dto.getTicketLink());
                    existing.setGreenVote(dto.getGreenVote());
                    existing.setRedVote(dto.getRedVote());

                    Ticket updated = ticketRepository.save(existing);
                    return ResponseEntity.ok(updated);
                }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
