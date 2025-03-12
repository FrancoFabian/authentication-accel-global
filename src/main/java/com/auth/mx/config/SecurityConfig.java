package com.auth.mx.config;

import com.auth.mx.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/bank/**").authenticated()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
/*

En la clase SecurityConfig, lo más común que tengas es la configuración de endpoints y quién puede acceder. Por ejemplo:

java
Copiar
Editar
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                // Ejemplo: si quieres que /api/admin/** requiera tener el rol ADMIN
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
Antes: Podrías haber tenido algo como .requestMatchers("/api/admin/**").hasRole("ADMIN") y dependías del enum Role.ROLE_ADMIN.
Ahora: Tus roles se guardan como strings (nombre = "ADMIN"). En User.getAuthorities(), convertimos nombre a "ROLE_ADMIN".
Así que está bien usar .hasAuthority("ROLE_ADMIN") o .hasRole("ADMIN") (Spring automáticamente añade ROLE_ si usas .hasRole("ADMIN")).
Verifica que no tengas referencias directas al enum en tu config. Por ejemplo, nada de Role.ROLE_ADMIN.
Si en tu User.getAuthorities() pones:

java
Copiar
Editar
SimpleGrantedAuthority("ROLE_" + role.getNombre())
entonces para el rol "ADMIN" que guardas en la BD, la GrantedAuthority final será "ROLE_ADMIN". Por ende, .hasRole("ADMIN") funcionará bien.

 */
