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

    // ---------- Telefone ----------

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    /** Para exibir: (44) 99709-3407 ou (44) 3622-1234. */
    public String getTelefoneFormatado() {
        return formatarTelefone(telefone);
    }

    public static String formatarTelefone(String telefone) {
        if (telefone == null) return "";
        String d = telefone.replaceAll("\\D", "");

        if (d.length() == 11) {
            return "(" + d.substring(0, 2) + ") " + d.substring(2, 7) + "-" + d.substring(7);
        }
        if (d.length() == 10) {
            return "(" + d.substring(0, 2) + ") " + d.substring(2, 6) + "-" + d.substring(6);
        }
        return telefone;
    }

    public static boolean telefoneValido(String telefone) {
        if (telefone == null) return false;
        int tamanho = telefone.replaceAll("\\D", "").length();
        return tamanho == 10 || tamanho == 11;
    }

    public static String telefoneSomenteDigitos(String telefone) {
        return telefone == null ? "" : telefone.replaceAll("\\D", "");
    }

    // ---------- Contato e endereço ----------

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