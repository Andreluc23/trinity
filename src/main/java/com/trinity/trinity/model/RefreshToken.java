package com.trinity.trinity.model;

import jakarta.persistence.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String token;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    protected RefreshToken() {
    }

    public RefreshToken(Usuario usuario, Duration validade) {
        this.token = UUID.randomUUID().toString();
        this.usuario = usuario;
        this.expiraEm = LocalDateTime.now().plus(validade);
    }

    public boolean isExpirado() {
        return LocalDateTime.now().isAfter(expiraEm);
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public LocalDateTime getExpiraEm() {
        return expiraEm;
    }
}