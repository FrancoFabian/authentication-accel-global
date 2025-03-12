package com.auth.mx.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private String password;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserTwoFactor userTwoFactor;


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<BankAccount> bankAccounts;

    // ================================
    // MÉTODOS AUXILIARES PARA 2FA
    // ================================

    // Getter y setter para "twoFactorEnabled"
    public boolean isTwoFactorEnabled() {
        // Si no existe userTwoFactor, asumimos "false"
        if (userTwoFactor == null) {
            return false;
        }
        return userTwoFactor.isTwoFactorEnabled();
    }

    public void setTwoFactorEnabled(boolean enabled) {
        if (userTwoFactor == null) {
            userTwoFactor = new UserTwoFactor();
            userTwoFactor.setUser(this);
        }
        userTwoFactor.setTwoFactorEnabled(enabled);
    }

    // Getter y setter para "twoFactorSecret"
    public String getTwoFactorSecret() {
        if (userTwoFactor == null) {
            return null;
        }
        return userTwoFactor.getTwoFactorSecret();
    }

    public void setTwoFactorSecret(String secret) {
        if (userTwoFactor == null) {
            userTwoFactor = new UserTwoFactor();
            userTwoFactor.setUser(this);
        }
        userTwoFactor.setTwoFactorSecret(secret);
    }

    // Getter y setter para "twoFactorMethod"
    public String getTwoFactorMethod() {
        if (userTwoFactor == null) {
            return null;
        }
        return userTwoFactor.getTwoFactorMethod();
    }

    public void setTwoFactorMethod(String method) {
        if (userTwoFactor == null) {
            userTwoFactor = new UserTwoFactor();
            userTwoFactor.setUser(this);
        }
        userTwoFactor.setTwoFactorMethod(method);
    }

    // Métodos de UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Si quieres que cada rol sea un GrantedAuthority:
        List<SimpleGrantedAuthority> roleAuthorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getNombre()))
                .collect(Collectors.toList());

        // Si además deseas exponer cada permiso como GrantedAuthority:
        // (Si tienes permisos en roles)
        List<SimpleGrantedAuthority> permissionAuthorities = roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(perm -> new SimpleGrantedAuthority(perm.getNombre()))
                .collect(Collectors.toList());

        // (Si quieres incluir permisos directos del usuario, si lo activaste)
//        List<SimpleGrantedAuthority> directPermAuthorities = directPermissions.stream()
//                .map(perm -> new SimpleGrantedAuthority(perm.getNombre()))
//                .collect(Collectors.toList());

        // Unir todo en una sola lista
        // (Si no usas permisos, puedes devolver solo la parte de roles)
        // roleAuthorities.addAll(permissionAuthorities);
        // roleAuthorities.addAll(directPermAuthorities);

        // Retorno un ejemplo combinando roles y permisos del rol
        roleAuthorities.addAll(permissionAuthorities);
        return roleAuthorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

