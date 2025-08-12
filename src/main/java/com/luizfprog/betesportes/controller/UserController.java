package com.luizfprog.betesportes.controller;

import com.luizfprog.betesportes.dto.RegistrationRequestDTO;
import com.luizfprog.betesportes.dto.UserResponseDTO;
import com.luizfprog.betesportes.entity.AppUser;
import com.luizfprog.betesportes.repository.AppUserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários")
@SecurityRequirement(name = "bearerAuth") // exige token JWT
public class UserController {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    @Operation(summary = "Listar usuários", description = "Retorna a lista de todos os usuários cadastrados.")
    public List<UserResponseDTO> listUsers() {
        return userRepository.findAll().stream()
                .map(UserResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Atualizar usuário", description = "Atualiza as informações de um usuário pelo ID.")
    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id,
                                                      @RequestBody RegistrationRequestDTO dto) {
        AppUser user = userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            user.setUsername(dto.getUsername());
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        // Atualiza companyName mesmo se for vazio (ou controle conforme sua regra)
        if (dto.getCompanyName() != null) {
            user.setCompanyName(dto.getCompanyName());
        }

        // Atualiza roles somente se vier no DTO
        if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
            Set<String> normalized = dto.getRoles().stream()
                    .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                    .collect(Collectors.toSet());
            user.setRoles(normalized);
        }

        AppUser saved = userRepository.save(user);
        return ResponseEntity.ok(new UserResponseDTO(saved));
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir usuário", description = "Remove um usuário pelo ID.")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(existing -> {
                    userRepository.delete(existing);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
