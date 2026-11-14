package com.trinity.trinity.model;

import com.trinity.trinity.util.Cpf;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "membros")
public class Membro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomeCompleto;

    @Column(nullable = false)
    private LocalDate dataNascimento;

    /** Guardado só com números (ex.: 52998224725). */
    @Column(nullable = false, unique = true)
    private String cpf;

    @Column(nullable = false)
    private String telefone;

    @Column(unique = true)
    private String email;

    @Embedded
    private Endereco endereco = new Endereco();

    @Column(nullable = false)
    private boolean ativo = true;

    public Membro() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    // ---------- CPF ----------

    /** Valor real, só com números: usado nas regras e nas buscas. */
    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    /** Para exibir: 529.982.247-25 (formulário de edição e perfil). */
    public String getCpfFormatado() {
        return Cpf.formatar(cpf);
    }

    /** Para exibir parcialmente oculto: ***.982.247-** (lista de membros). */
    public String getCpfMascarado() {
        return Cpf.mascarar(cpf);
    }

    // ---------- Contato e endereço ----------

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Quando todas as colunas do endereço estão vazias, o Hibernate devolve null:
     * aqui sempre volta um endereço (vazio, se for o caso), para o formulário não quebrar.
     */
    public Endereco getEndereco() {
        if (endereco == null) {
            endereco = new Endereco();
        }
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}