package com.luizfprog.betesportes.controller;

import com.luizfprog.betesportes.dto.LoginRequestDTO;
import com.luizfprog.betesportes.dto.RegistrationRequestDTO; // Import adicionado
import com.luizfprog.betesportes.dto.UserResponseDTO;
import com.luizfprog.betesportes.entity.AppUser;
import com.luizfprog.betesportes.repository.AppUserRepository; // Import adicionado
import com.luizfprog.betesportes.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder; // Import adicionado
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authManager;
    @Autowired
    private JwtUtil jwtUtil;

    // Novas injeções
    @Autowired
    private AppUserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDTO request) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        String token = jwtUtil.generateToken((UserDetails) auth.getPrincipal());
        return ResponseEntity.ok(token);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegistrationRequestDTO request, Authentication authentication) {
            boolean isAdmin = false;

            // Verifica se há autenticação (usuário logado)
            if (authentication != null) {
                isAdmin = authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            }

            // Se tentar registrar ADMIN sem ser admin
            if (request.getRoles() != null &&
                    request.getRoles().contains("ADMIN") &&
                    !isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Somente administradores podem criar usuários ADMIN");
            }

        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Usuário já existe");
        }

        AppUser newUser = new AppUser();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        // Valida e define as roles (com fallback para USER)
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            newUser.setRoles(Set.of("USER")); // Default
        } else {
            // Garante que apenas roles válidas sejam atribuídas
            Set<String> validRoles = request.getRoles().stream()
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                    .collect(Collectors.toSet());
            newUser.setRoles(validRoles);
        }

        userRepository.save(newUser);
        return ResponseEntity.ok("Usuário registrado com sucesso");
    }

    @PostMapping("/admin/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> registerAdmin(@RequestBody RegistrationRequestDTO request) {
        // Mesma lógica de registro, mas garantindo role ADMIN
        if (!request.getRoles().contains("ADMIN")) {
            request.setRoles(Set.of("ADMIN")); // Força role ADMIN
        }
        return register(request, SecurityContextHolder.getContext().getAuthentication());
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(Authentication auth) {
        AppUser user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        return ResponseEntity.ok(new UserResponseDTO(user));
    }
}