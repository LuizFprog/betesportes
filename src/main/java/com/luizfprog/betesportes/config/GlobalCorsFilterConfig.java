package com.luizfprog.betesportes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class GlobalCorsFilterConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsFilter corsFilter() {
        CorsConfiguration cfg = new CorsConfiguration();

        // 1) Permite suas origens exatas
        cfg.setAllowedOrigins(List.of(
                "https://tabela-sports-office.lovable.app",
                "https://promo.apostaganha.bet.br",
                "http://localhost:3000"
        ));
        // 2) Se precisar de curingas, troque por:
        // cfg.setAllowedOriginPatterns(List.of("https://promo.apostaganha.bet.br/*", "..."));

        // 3) Métodos, cabeçalhos e credenciais
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);

        // 4) Registra pra todas as rotas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);

        return new CorsFilter(source);
    }
}
