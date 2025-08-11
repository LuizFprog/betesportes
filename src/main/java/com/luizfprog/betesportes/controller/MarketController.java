package com.luizfprog.betesportes.controller;

import com.luizfprog.betesportes.dto.MarketRequestDTO;
import com.luizfprog.betesportes.entity.AppUser;
import com.luizfprog.betesportes.entity.Market;
import com.luizfprog.betesportes.repository.AppUserRepository;
import com.luizfprog.betesportes.repository.MarketRepository;
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
@RequestMapping("/markets")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "https://tabela-sports-office.lovable.app",
        "https://promo.apostaganha.bet.br/app",
        "http://localhost:3000"
}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@Tag(name = "Markets", description = "Gerenciamento de mercados")
public class MarketController {

    private final MarketRepository marketRepository;
    private final AppUserRepository userRepository;

    @Operation(summary = "Criar mercado", description = "Cria um novo market — requer role ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Market criado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Market> createMarket(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do market", required = true)
            @RequestBody MarketRequestDTO dto,
            @Parameter(hidden = true) Authentication auth
    ) {
        AppUser user = userRepository
                .findByUsername(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        Market market = new Market();
        market.setName(dto.getName());
        market.setChoices(dto.getChoices());
        market.setOwner(user);
        Market saved = marketRepository.save(market);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(summary = "Listar markets", description = "Retorna todos os markets.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada")
    })
    @GetMapping
    public List<Market> getAllMarkets() {
        return marketRepository.findAll();
    }

    @Operation(summary = "Atualizar market", description = "Atualiza market — requer role ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Market atualizado"),
            @ApiResponse(responseCode = "404", description = "Market não encontrado")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Market> updateMarket(
            @Parameter(description = "ID do market", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados atualizados do market", required = true)
            @RequestBody MarketRequestDTO dto,
            @Parameter(hidden = true) Authentication auth
    ) {
        return marketRepository.findById(id)
                .map(existing -> {
                    existing.setName(dto.getName());
                    existing.setChoices(dto.getChoices());
                    Market updated = marketRepository.save(existing);
                    return ResponseEntity.ok(updated);
                }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Excluir market", description = "Exclui um market — requer role ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Market excluído"),
            @ApiResponse(responseCode = "404", description = "Market não encontrado")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteMarket(
            @Parameter(description = "ID do market", required = true) @PathVariable Long id,
            @Parameter(hidden = true) Authentication auth
    ) {
        return marketRepository.findById(id)
                .map(existing -> {
                    marketRepository.delete(existing);
                    return ResponseEntity.noContent().<Void>build();
                }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
