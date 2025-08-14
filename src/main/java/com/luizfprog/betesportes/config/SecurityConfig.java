package com.luizfprog.betesportes.config;

import com.luizfprog.betesportes.security.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@EnableMethodSecurity
@EnableWebSecurity
@Configuration
public class SecurityConfig {
    @Autowired
    private JwtFilter jwtFilter;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers("/auth/register").permitAll()
                        .requestMatchers("/auth/admin/**").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/markets/**", "/teams/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // GET  -> ADMIN, MANAGER, USER, EMPLOYEE
                        .requestMatchers(HttpMethod.GET,
                                "/matches/**", "/bets/**", "/tickets/**", "/offers/**")
                        .hasAnyRole("ADMIN","MANAGER","USER","EMPLOYEE")

                        // POST -> ADMIN, MANAGER, EMPLOYEE (USER NÃO POST)
                        .requestMatchers(HttpMethod.POST,
                                "/matches/**", "/bets/**", "/tickets/**", "/offers/**")
                        .hasAnyRole("ADMIN","MANAGER","EMPLOYEE")

                        // PUT -> ADMIN, MANAGER, USER (EMPLOYEE NÃO PUT)
                        .requestMatchers(HttpMethod.PUT,
                                "/matches/**", "/bets/**", "/tickets/**", "/offers/**")
                        .hasAnyRole("ADMIN","MANAGER","USER")

                        // DELETE -> ADMIN, MANAGER
                        .requestMatchers(HttpMethod.DELETE,
                                "/matches/**", "/bets/**", "/tickets/**", "/offers/**")
                        .hasAnyRole("ADMIN","MANAGER")

                        // USERS (controller /users)
                        // Manager deve poder CRUD em users; Employee não deve acessar
                        .requestMatchers(HttpMethod.GET, "/users/**").hasAnyRole("ADMIN","MANAGER")
                        .requestMatchers(HttpMethod.POST, "/users/**").hasAnyRole("ADMIN","MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/users/**").hasAnyRole("ADMIN","MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/users/**").hasAnyRole("ADMIN","MANAGER")
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(List.of(
                "https://tabela-sports-office.lovable.app",
                "https://promo.apostaganha.bet.br/app",
                "http://localhost:3000",
                "https://palpites-ag.vercel.app/"
        ));
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("*"));
        cors.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // aplica a todos endpoints
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

