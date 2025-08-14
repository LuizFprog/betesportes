package com.luizfprog.betesportes.controller;

import com.luizfprog.betesportes.dto.OfferRequestDTO;
import com.luizfprog.betesportes.entity.AppUser;
import com.luizfprog.betesportes.entity.Offer;
import com.luizfprog.betesportes.repository.AppUserRepository;
import com.luizfprog.betesportes.repository.OfferRepository;
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
@RequestMapping("/offers")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "https://tabela-sports-office.lovable.app",
        "https://promo.apostaganha.bet.br/app",
        "http://localhost:3000",
        "https://palpites-ag.vercel.app/"
}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@Tag(name = "Offers", description = "Promoções e ofertas")
public class OfferController {

    private final OfferRepository offerRepository;
    private final AppUserRepository userRepository;

    @Operation(summary = "Criar oferta", description = "Cria uma oferta associada ao usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Oferta criada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Offer> createOffer(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados da oferta", required = true)
            @RequestBody OfferRequestDTO dto,
            @Parameter(hidden = true) Authentication auth
    ) {
        AppUser user = userRepository
                .findByUsername(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        Offer offer = new Offer();
        offer.setName(dto.getName());
        offer.setOfferDescription(dto.getOfferDescription());
        offer.setOfferImageLink(dto.getOfferImageLink());
        offer.setOfferButtonLink(dto.getOfferButtonLink());
        offer.setRulesTitle(dto.getRulesTitle());
        offer.setRulesSubTitle(dto.getRulesSubTitle());
        offer.setRulesParagraphs(dto.getRulesParagraphs());
        offer.setRulesGIFLinks(dto.getRulesGIFLinks());
        offer.setOwner(user);

        Offer saved = offerRepository.save(offer);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(summary = "Listar ofertas", description = "Retorna ofertas do usuário (ou todas se for ADMIN).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    public List<Offer> getAllOffers(@Parameter(hidden = true) Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return offerRepository.findAll();
        }
        AppUser currentUser = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        String company = currentUser.getCompanyName();
        if (company == null) {
            return List.of();
        }
        return offerRepository.findByOwnerCompanyName(company);
    }

    @Operation(summary = "Atualizar oferta", description = "Atualiza oferta; apenas ADMIN ou dono da empresa podem atualizar.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Oferta atualizada"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Oferta não encontrada")
    })
    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Offer> updateOffer(
            @Parameter(description = "ID da oferta", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados atualizados da oferta", required = true)
            @RequestBody OfferRequestDTO dto,
            @Parameter(hidden = true) Authentication auth
    ) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return offerRepository.findById(id)
                .map(existing -> {
                    if (!isAdmin) {
                        AppUser currentUser = userRepository.findByUsername(auth.getName())
                                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
                        String userCompany = currentUser.getCompanyName();
                        String ownerCompany = existing.getOwner().getCompanyName();
                        if (ownerCompany == null || !ownerCompany.equals(userCompany)) {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).<Offer>build();
                        }
                    }

                    existing.setName(dto.getName());
                    existing.setOfferDescription(dto.getOfferDescription());
                    existing.setOfferImageLink(dto.getOfferImageLink());
                    existing.setOfferButtonLink(dto.getOfferButtonLink());
                    existing.setRulesTitle(dto.getRulesTitle());
                    existing.setRulesSubTitle(dto.getRulesSubTitle());
                    existing.setRulesParagraphs(dto.getRulesParagraphs());
                    existing.setRulesGIFLinks(dto.getRulesGIFLinks());

                    Offer updated = offerRepository.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Excluir oferta", description = "Exclui oferta; apenas ADMIN ou dono da empresa podem excluir.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Oferta excluída"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Oferta não encontrada")
    })
    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteOffer(
            @Parameter(description = "ID da oferta", required = true) @PathVariable Long id,
            @Parameter(hidden = true) Authentication auth
    ) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return offerRepository.findById(id)
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
                    offerRepository.delete(existing);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
