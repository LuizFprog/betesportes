package com.luizfprog.betesportes.controller;

import com.luizfprog.betesportes.dto.OfferRequestDTO;
import com.luizfprog.betesportes.entity.Offer;
import com.luizfprog.betesportes.repository.OfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/offers")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "https://tabela-sports-office.lovable.app",
        "https://promo.apostaganha.bet.br/app",
        "http://localhost:3000"
        },
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class OfferController {
    @Autowired
    private OfferRepository offerRepository;

    @PostMapping
    public Offer createOffer(@RequestBody OfferRequestDTO dto) {
        Offer offer = new Offer();
        offer.setName(dto.getName());
        offer.setOfferDescription(dto.getOfferDescription());
        offer.setOfferImageLink(dto.getOfferImageLink());
        offer.setOfferButtonLink(dto.getOfferButtonLink());
        offer.setRulesTitle(dto.getRulesTitle());
        offer.setRulesSubTitle(dto.getRulesSubTitle());
        offer.setRulesParagraphs(dto.getRulesParagraphs());
        offer.setRulesGIFLinks(dto.getRulesGIFLinks());

        return offerRepository.save(offer);
    }

    @GetMapping
    public List<Offer> getAllOffers() { return offerRepository.findAll(); }

    @DeleteMapping("/{id}")
    public void deleteOffer(@PathVariable Long id) { offerRepository.deleteById(id); }

    @PutMapping("/{id}")
    public ResponseEntity<Offer> updateOffer (@PathVariable Long id, @RequestBody OfferRequestDTO dto) {
        return offerRepository.findById(id)
                .map(existing -> {
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
                }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
