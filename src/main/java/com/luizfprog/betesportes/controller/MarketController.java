package com.luizfprog.betesportes.controller;

import com.luizfprog.betesportes.dto.MarketRequestDTO;
import com.luizfprog.betesportes.entity.Market;
import com.luizfprog.betesportes.repository.MarketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/markets")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8081",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class MarketController {
    @Autowired
    private MarketRepository marketRepository;

    @PostMapping
    public Market createMarket(@RequestBody MarketRequestDTO dto) {
        Market market = new Market();
        market.setName(dto.getName());
        market.setChoices(dto.getChoices());
        return marketRepository.save(market);
    }

    @GetMapping
    public List<Market> getAllMarkets() { return marketRepository.findAll(); }

    @DeleteMapping("/{id}")
    public void deleteMarket(@PathVariable Long id) { marketRepository.deleteById(id); }

    @PutMapping("/{id}")
    public ResponseEntity<Market> updateMarket (@PathVariable Long id, @RequestBody MarketRequestDTO dto) {
        return marketRepository.findById(id)
                .map(existing -> {
                    existing.setName(dto.getName());
                    existing.setChoices(dto.getChoices());

                    Market updated = marketRepository.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
