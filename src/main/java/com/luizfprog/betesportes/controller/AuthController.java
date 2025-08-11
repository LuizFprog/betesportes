package com.luizfprog.betesportes.controller;

import com.luizfprog.betesportes.dto.LoginRequestDTO;
import com.luizfprog.betesportes.dto.RegistrationRequestDTO;
import com.luizfprog.betesportes.dto.UserResponseDTO;
import com.luizfprog.betesportes.entity.AppUser;
import com.luizfprog.betesportes.repository.AppUserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Endpoints de autenticação e gerenciamento de usuários")
public class AuthController {
    @Autowired
    private AuthenticationManager authManager;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AppUserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Operation(summary = "Login", description = "Autentica o usuário e retorna um token JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token JWT gerado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<String> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Credenciais do usuário", required = true)
            @RequestBody LoginRequestDTO request) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        String token = jwtUtil.generateToken((UserDetails) auth.getPrincipal());
        return ResponseEntity.ok(token);
    }

    @Operation(summary = "Registrar usuário", description = "Registra um novo usuário. Para criar um ADMIN, o chamador precisa ser ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Usuário já existe / requisição inválida"),
            @ApiResponse(responseCode = "403", description = "Tentativa de criar ADMIN sem permissão")
    })
    @PostMapping("/register")
    public ResponseEntity<String> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados para registro", required = true)
            @RequestBody RegistrationRequestDTO request,
            @Parameter(hidden = true) Authentication authentication) {
        boolean isAdmin = false;

        if (authentication != null) {
            isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        }

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
        newUser.setCompanyName(request.getCompanyName());

        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            newUser.setRoles(Set.of("USER"));
        } else {
            Set<String> validRoles = request.getRoles().stream()
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                    .collect(Collectors.toSet());
            newUser.setRoles(validRoles);
        }

        userRepository.save(newUser);
        return ResponseEntity.ok("Usuário registrado com sucesso");
    }

    @Operation(summary = "Registrar ADMIN", description = "Cria um usuário com role ADMIN. Requer role ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário ADMIN registrado"),
            @ApiResponse(responseCode = "403", description = "Necessário role ADMIN")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/register")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<String> registerAdmin(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados para registro ADMIN", required = true)
            @RequestBody RegistrationRequestDTO request) {
        if (!request.getRoles().contains("ADMIN")) {
            request.setRoles(Set.of("ADMIN"));
        }
        return register(request, SecurityContextHolder.getContext().getAuthentication());
    }

    @Operation(summary = "Informações do usuário atual", description = "Retorna informações do usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserResponseDTO> getCurrentUser(@Parameter(hidden = true) Authentication auth) {
        AppUser user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        return ResponseEntity.ok(new UserResponseDTO(user));
    }
}
