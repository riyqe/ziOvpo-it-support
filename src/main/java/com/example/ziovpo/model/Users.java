package com.example.ziovpo.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class Users implements UserDetails {

    @Id
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    @JsonAlias({"username", "name"})
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    private String department;

    @Column(name = "password_hash", nullable = false)
    @JsonAlias({"password", "password_hash"})
    private String password;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "is_account_expired", nullable = false)
    @Builder.Default
    private boolean isAccountExpired = false;

    @Column(name = "is_account_locked", nullable = false)
    @Builder.Default
    private boolean isAccountLocked = false;

    @Column(name = "is_credentials_expired", nullable = false)
    @Builder.Default
    private boolean isCredentialsExpired = false;

    @Column(name = "is_disabled", nullable = false)
    @Builder.Default
    private boolean isDisabled = false;

    @PrePersist
    @SuppressWarnings("unused")
    private void ensureId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) {
            return Collections.emptyList();
        }
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !isAccountExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isAccountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !isCredentialsExpired;
    }

    @Override
    public boolean isEnabled() {
        return !isDisabled;
    }
}
