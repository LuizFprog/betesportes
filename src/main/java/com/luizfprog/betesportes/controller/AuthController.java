package com.luizfprog.betesportes.controller;

import com.luizfprog.betesportes.dto.LoginRequestDTO;
import com.luizfprog.betesportes.dto.RegistrationRequestDTO;
import com.luizfprog.betesportes.dto.TokenResponseDTO;
import com.luizfprog.betesportes.dto.UserResponseDTO;
import com.luizfprog.betesportes.entity.AppUser;
import com.luizfprog.betesportes.entity.RefreshToken;
import com.luizfprog.betesportes.repository.AppUserRepository;
import com.luizfprog.betesportes.service.AppUserDetailsService;
import com.luizfprog.betesportes.service.RefreshTokenService;
import com.luizfprog.betesportes.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private AppUserDetailsService userDetailsService;

    @Value("${refresh.token.days:30}")
    private long refreshTokenDays;

    @Operation(summary = "Login", description = "Autentica o usuário e retorna um token JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token JWT gerado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody LoginRequestDTO request) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String accessToken = jwtUtil.generateToken(userDetails); // seu método já existente

        // pega AppUser para criar refresh token (assume username unico)
        AppUser user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow();

        var refresh = refreshTokenService.createRefreshToken(user);

        // cria cookie HttpOnly; ajuste sameSite conforme necessidade (None p/ cross-site)
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refresh.getToken())
                .httpOnly(true)
                .secure(true) // em prod: true; em dev, se não usar https precise ajustar
                .path("/")
                .maxAge(refreshTokenDays * 24 * 60 * 60)
                .sameSite("None") // necessário se front-end está em outro domínio
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new TokenResponseDTO(accessToken, jwtUtil.getExpiresInSeconds()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refresh(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        RefreshToken stored = refreshTokenService.findByToken(refreshToken);
        if (refreshTokenService.isExpiredOrRevoked(stored)) {
            // opcional: apagar token do DB se expirado
            if (stored != null) refreshTokenService.revoke(stored);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // rotaciona: revoga o antigo e cria um novo
        RefreshToken newRefresh = refreshTokenService.rotate(stored);

        // cria novo access token
        AppUser user = newRefresh.getUser();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String newAccessToken = jwtUtil.generateToken(userDetails);

        // set cookie do novo refresh
        ResponseCookie cookie = ResponseCookie.from("refreshToken", newRefresh.getToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenDays * 24 * 60 * 60)
                .sameSite("None")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new TokenResponseDTO(newAccessToken, jwtUtil.getExpiresInSeconds()));
    }

    @Operation(summary = "Registrar usuário", description = "Registra um novo usuário. Para criar um ADMIN, o chamador precisa ser ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Usuário já existe / requisição inválida"),
            @ApiResponse(responseCode = "403", description = "Tentativa de criar ADMIN sem permissão")
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(
            @RequestBody RegistrationRequestDTO request,
            @Parameter(hidden = true) Authentication authentication) {

        boolean isAdmin = false;
        boolean isManager = false;
        if (authentication != null) {
            isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            isManager = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));
        }

        // Normaliza roles solicitadas para comparação (ex: "ROLE_ADMIN" -> "ADMIN")
        Set<String> requestedRoles = (request.getRoles() == null || request.getRoles().isEmpty())
                ? Set.of("USER")
                : request.getRoles().stream()
                .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());

        // Caso: requisição NÃO autenticada -> permitir apenas registro público de USER
        if (authentication == null) {
            boolean onlyUser = requestedRoles.size() == 1 && requestedRoles.contains("USER");
            if (!onlyUser) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            // segue para criação do usuário comum público
        } else {
            // Requisição autenticada: apenas ADMIN ou MANAGER podem usar esse endpoint para criar usuários
            if (!isAdmin && !isManager) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Se solicitou ADMIN ou MANAGER e o chamador não for ADMIN -> proibido
            if (requestedRoles.stream().anyMatch(r -> r.equals("ADMIN") || r.equals("MANAGER")) && !isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Se chamador é MANAGER (e não ADMIN), aplicar restrições do manager
            if (isManager && !isAdmin) {
                // manager só pode criar USER ou EMPLOYEE
                boolean allowedForManager = requestedRoles.stream().allMatch(r -> r.equals("USER") || r.equals("EMPLOYEE"));
                if (!allowedForManager) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }

                // Recupera dados do próprio manager
                AppUser me = userRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

                String myCompany = me.getCompanyName();
                if (myCompany == null) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }

                // Se request não informou companyName, atribuimos a mesma do manager; se informou diferente -> proibido
                if (request.getCompanyName() == null) {
                    request.setCompanyName(myCompany);
                } else if (!request.getCompanyName().equals(myCompany)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
        }

        // verifica se username já existe
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().build();
        }

        // cria usuário
        AppUser newUser = new AppUser();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setCompanyName(request.getCompanyName());

        // normaliza para salvar no formato ROLE_*
        Set<String> rolesToSave = (request.getRoles() == null || request.getRoles().isEmpty())
                ? Set.of("ROLE_USER")
                : request.getRoles().stream()
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .collect(Collectors.toSet());

        newUser.setRoles(rolesToSave);

        AppUser saved = userRepository.save(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserResponseDTO(saved));
    }

    @Operation(summary = "Registrar ADMIN", description = "Cria um usuário com role ADMIN. Requer role ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário ADMIN registrado"),
            @ApiResponse(responseCode = "403", description = "Necessário role ADMIN")
    })
    @PostMapping("/admin/register")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserResponseDTO> registerAdmin(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados para registro ADMIN", required = true)
            @RequestBody RegistrationRequestDTO request) {
        if (request.getRoles() == null || !request.getRoles().contains("ADMIN")) {
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

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        if (refreshToken != null) {
            RefreshToken stored = refreshTokenService.findByToken(refreshToken);
            if (stored != null) refreshTokenService.revoke(stored);
        }
        // limpa cookie no cliente
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
}
