package com.auth.mx.service;

import com.auth.mx.dto.AuthRequest;
import com.auth.mx.dto.AuthResponse;
import com.auth.mx.model.Role;
import com.auth.mx.model.User;
import com.auth.mx.model.UserTwoFactor;
import com.auth.mx.repository.RoleRepository;
import com.auth.mx.repository.UserRepository;
import com.auth.mx.repository.UserTwoFactorRepository;
import com.auth.mx.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserTwoFactorRepository userTwoFactorRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(AuthRequest request) {
        // Validar si el usuario ya existe (opcional)
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("El usuario ya existe");
        }
// Recoger el rol por nombre (por ejemplo "USER") de la BD
        Role defaultRole = roleRepository.findByNombre("USER")
                .orElseThrow(() -> new RuntimeException("No existe rol USER en la BD"));

        User user = User.builder()
                .email(request.getEmail())
                .numberPhone(request.getNumberPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                // Set vacío de inicio, luego agregamos
                .roles(new HashSet<>())
                .build();

       // Agregar rol por defecto
        user.getRoles().add(defaultRole);

        // Guardar en la BD
        userRepository.save(user);

        // Generar token JWT para el usuario registrado
        String token = jwtService.generateToken(user);
        // Convertir roles a una lista de strings
        List<String> rolesEnTexto = user.getRoles().stream()
                .map(Role::getNombre)
                .collect(Collectors.toList());

        // Retornar respuesta
        return AuthResponse.builder()
                .email(user.getEmail())
                .numberPhone(user.getNumberPhone())
                .token(token)
                .role(rolesEnTexto)
                .build();
    }

    public AuthResponse login(AuthRequest request) {
        // Buscar usuario por email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // Validar contraseña
        if (!passwordEncoder
                .matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        // 2) Buscar config 2FA
        UserTwoFactor twoFactorConfig = userTwoFactorRepository.findByUserId(user.getId())
                .orElse(null);

        // 2) Si twoFactorEnabled == true, no emitimos JWT de una
        if (user.isTwoFactorEnabled()) {
            return AuthResponse.builder()
                    .email(user.getEmail())
                    .numberPhone(user.getNumberPhone())
                    .twoFactorRequired(true)  // nuevo flag
                    .build();
        }

        // Generar token JWT para el usuario autenticado
        String token = jwtService.generateToken(user);

        // Convertir roles a una lista de strings
        List<String> rolesEnTexto = user.getRoles().stream()
                .map(Role::getNombre)
                .collect(Collectors.toList());

        // Construir respuesta
        return AuthResponse.builder()
                .email(user.getEmail())
                .numberPhone(user.getNumberPhone())
                .token(token)
                .role(rolesEnTexto)
                .build();
    }

}
