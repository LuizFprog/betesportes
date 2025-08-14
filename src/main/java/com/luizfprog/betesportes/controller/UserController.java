package com.luizfprog.betesportes.controller;

import com.luizfprog.betesportes.dto.RegistrationRequestDTO;
import com.luizfprog.betesportes.dto.UserResponseDTO;
import com.luizfprog.betesportes.entity.AppUser;
import com.luizfprog.betesportes.repository.AppUserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
// permite apenas ADMIN e MANAGER chegarem a esse controller — checagens adicionais por método
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários")
public class UserController {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // utilitários
    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean isManager(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));
    }

    private AppUser getCurrentAppUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }

    @GetMapping
    @Operation(summary = "Listar usuários", description = "Admin vê todos; Manager vê só usuários da mesma company.")
    public ResponseEntity<List<UserResponseDTO>> listUsers(Authentication auth) {
        if (isAdmin(auth)) {
            List<UserResponseDTO> all = userRepository.findAll().stream()
                    .map(UserResponseDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(all);
        }

        if (isManager(auth)) {
            AppUser me = getCurrentAppUser(auth);
            String company = me.getCompanyName();
            if (company == null) {
                return ResponseEntity.ok(List.of());
            }
            // se você adicionou findByCompanyName no repo, use-o; aqui uso filter por segurança
            List<UserResponseDTO> filtered = userRepository.findAll().stream()
                    .filter(u -> company.equals(u.getCompanyName()))
                    .map(UserResponseDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(filtered);
        }

        // caso chegue aqui, não está autorizado por regra de negócio
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PostMapping
    @Operation(summary = "Criar usuário", description = "Admin cria qualquer usuário. Manager só cria usuários da sua company e não pode criar ADMIN.")
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody RegistrationRequestDTO dto, Authentication auth) {
        boolean admin = isAdmin(auth);
        boolean manager = isManager(auth);

        // evita sobrescrever ADMIN por manager
        if (manager) {
            // manager não pode criar ADMIN
            if (dto.getRoles() != null && dto.getRoles().stream().anyMatch(r -> r.equalsIgnoreCase("ADMIN") || r.equalsIgnoreCase("ROLE_ADMIN"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // garante que o usuário criado pertença à mesma company do manager
            AppUser me = getCurrentAppUser(auth);
            String myCompany = me.getCompanyName();
            if (dto.getCompanyName() == null) {
                dto.setCompanyName(myCompany); // assume company do manager
            } else if (!dto.getCompanyName().equals(myCompany)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        if (userRepository.existsByUsername(dto.getUsername())) {
            return ResponseEntity.badRequest().build();
        }

        AppUser newUser = new AppUser();
        newUser.setUsername(dto.getUsername());
        newUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        newUser.setCompanyName(dto.getCompanyName());

        if (dto.getRoles() == null || dto.getRoles().isEmpty()) {
            newUser.setRoles(Set.of("ROLE_USER"));
        } else {
            Set<String> validRoles = dto.getRoles().stream()
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                    .collect(Collectors.toSet());
            newUser.setRoles(validRoles);
        }

        AppUser saved = userRepository.save(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserResponseDTO(saved));
    }

    @Operation(summary = "Atualizar usuário", description = "Admin pode atualizar qualquer usuário. Manager só atualiza usuários da mesma company e não pode conceder ADMIN.")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id,
                                                      @RequestBody RegistrationRequestDTO dto,
                                                      Authentication auth) {
        boolean admin = isAdmin(auth);
        boolean manager = isManager(auth);

        return userRepository.findById(id)
                .map(user -> {
                    if (!admin) {
                        if (manager) {
                            AppUser me = getCurrentAppUser(auth);
                            String myCompany = me.getCompanyName();
                            String ownerCompany = user.getCompanyName();
                            if (ownerCompany == null || myCompany == null || !ownerCompany.equals(myCompany)) {
                                return ResponseEntity.status(HttpStatus.FORBIDDEN).<UserResponseDTO>build();
                            }
                            // manager não pode atribuir role ADMIN
                            if (dto.getRoles() != null && dto.getRoles().stream()
                                    .anyMatch(r -> r.equalsIgnoreCase("ADMIN") || r.equalsIgnoreCase("ROLE_ADMIN"))) {
                                return ResponseEntity.status(HttpStatus.FORBIDDEN).<UserResponseDTO>build();
                            }
                            // manager não pode mudar company para outra
                            if (dto.getCompanyName() != null && !dto.getCompanyName().equals(myCompany)) {
                                return ResponseEntity.status(HttpStatus.FORBIDDEN).<UserResponseDTO>build();
                            }
                        } else {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).<UserResponseDTO>build();
                        }
                    }

                    // atualização permitida (admin ou manager com same-company)
                    if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
                        user.setUsername(dto.getUsername());
                    }
                    if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
                        user.setPassword(passwordEncoder.encode(dto.getPassword()));
                    }
                    if (dto.getCompanyName() != null) {
                        user.setCompanyName(dto.getCompanyName());
                    }
                    if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
                        Set<String> normalized = dto.getRoles().stream()
                                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                                .collect(Collectors.toSet());
                        user.setRoles(normalized);
                    }

                    AppUser saved = userRepository.save(user);
                    return ResponseEntity.ok(new UserResponseDTO(saved));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir usuário", description = "Admin pode excluir qualquer usuário. Manager só exclui usuários da mesma company.")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, Authentication auth) {
        boolean admin = isAdmin(auth);
        boolean manager = isManager(auth);

        return userRepository.findById(id)
                .map(existing -> {
                    if (!admin) {
                        if (manager) {
                            AppUser me = getCurrentAppUser(auth);
                            String myCompany = me.getCompanyName();
                            String ownerCompany = existing.getCompanyName();
                            if (ownerCompany == null || myCompany == null || !ownerCompany.equals(myCompany)) {
                                return ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build();
                            }
                        } else {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build();
                        }
                    }
                    userRepository.delete(existing);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
