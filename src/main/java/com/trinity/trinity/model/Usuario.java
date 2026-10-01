package com.trinity.trinity.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(nullable = false)
    private boolean admin = false;

    @Column(nullable = false)
    private boolean acessoMembros = false;

    @Column(nullable = false)
    private boolean acessoAvisos = false;

    @Column(nullable = false)
    private boolean acessoEventos = false;

    @Column(nullable = false)
    private boolean acessoEscalas = false;

    @Column(nullable = false)
    private boolean acessoPatrimonio = false;

    @Column(nullable = false)
    private boolean acessoFinanceiro = false;

    public Usuario() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public boolean isAcessoMembros() {
        return acessoMembros;
    }

    public void setAcessoMembros(boolean acessoMembros) {
        this.acessoMembros = acessoMembros;
    }

    public boolean isAcessoAvisos() {
        return acessoAvisos;
    }

    public void setAcessoAvisos(boolean acessoAvisos) {
        this.acessoAvisos = acessoAvisos;
    }

    public boolean isAcessoEventos() {
        return acessoEventos;
    }

    public void setAcessoEventos(boolean acessoEventos) {
        this.acessoEventos = acessoEventos;
    }

    public boolean isAcessoEscalas() {
        return acessoEscalas;
    }

    public void setAcessoEscalas(boolean acessoEscalas) {
        this.acessoEscalas = acessoEscalas;
    }

    public boolean isAcessoPatrimonio() {
        return acessoPatrimonio;
    }

    public void setAcessoPatrimonio(boolean acessoPatrimonio) {
        this.acessoPatrimonio = acessoPatrimonio;
    }

    public boolean isAcessoFinanceiro() {
        return acessoFinanceiro;
    }

    public void setAcessoFinanceiro(boolean acessoFinanceiro) {
        this.acessoFinanceiro = acessoFinanceiro;
    }
}