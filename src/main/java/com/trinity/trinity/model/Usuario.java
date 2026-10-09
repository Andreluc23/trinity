package com.trinity.trinity.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "membro_id", nullable = false, unique = true)
    private Membro membro;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(nullable = false)
    private boolean admin = false;

    @Column(nullable = false)
    private boolean gerenciarMembros = false;

    @Column(nullable = false)
    private boolean gerenciarAvisos = false;

    @Column(nullable = false)
    private boolean gerenciarEventos = false;

    @Column(nullable = false)
    private boolean acessoEscalas = false;

    @Column(nullable = false)
    private boolean gerenciarEscalas = false;

    @Column(nullable = false)
    private boolean gerenciarPatrimonio = false;

    @Column(nullable = false)
    private boolean gerenciarFinanceiro = false;

    public Usuario() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Membro getMembro() {
        return membro;
    }

    public void setMembro(Membro membro) {
        this.membro = membro;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isGerenciarMembros() {
        return gerenciarMembros;
    }

    public void setGerenciarMembros(boolean gerenciarMembros) {
        this.gerenciarMembros = gerenciarMembros;
    }

    public boolean isGerenciarAvisos() {
        return gerenciarAvisos;
    }

    public void setGerenciarAvisos(boolean gerenciarAvisos) {
        this.gerenciarAvisos = gerenciarAvisos;
    }

    public boolean isGerenciarEventos() {
        return gerenciarEventos;
    }

    public void setGerenciarEventos(boolean gerenciarEventos) {
        this.gerenciarEventos = gerenciarEventos;
    }

    public boolean isAcessoEscalas() {
        return acessoEscalas;
    }

    public void setAcessoEscalas(boolean acessoEscalas) {
        this.acessoEscalas = acessoEscalas;
    }

    public boolean isGerenciarEscalas() {
        return gerenciarEscalas;
    }

    public void setGerenciarEscalas(boolean gerenciarEscalas) {
        this.gerenciarEscalas = gerenciarEscalas;
    }

    public boolean isGerenciarPatrimonio() {
        return gerenciarPatrimonio;
    }

    public void setGerenciarPatrimonio(boolean gerenciarPatrimonio) {
        this.gerenciarPatrimonio = gerenciarPatrimonio;
    }

    public boolean isGerenciarFinanceiro() {
        return gerenciarFinanceiro;
    }

    public void setGerenciarFinanceiro(boolean gerenciarFinanceiro) {
        this.gerenciarFinanceiro = gerenciarFinanceiro;
    }
}