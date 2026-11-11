package com.trinity.trinity.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Endereço gravado nas próprias colunas da tabela de quem o usa (não é uma tabela separada).
 */
@Embeddable
public class Endereco {

    @Column(length = 8)
    private String cep;

    private String logradouro;

    @Column(length = 10)
    private String numero;

    private String complemento;

    private String bairro;

    private String cidade;

    @Column(length = 2)
    private String uf;

    /**
     * Padroniza antes de gravar: CEP só com números, UF em maiúsculas e campos vazios como nulos.
     */
    public void normalizar() {
        cep = vazioComoNulo(cep == null ? null : cep.replaceAll("\\D", ""));
        logradouro = vazioComoNulo(logradouro);
        numero = vazioComoNulo(numero);
        complemento = vazioComoNulo(complemento);
        bairro = vazioComoNulo(bairro);
        cidade = vazioComoNulo(cidade);
        uf = uf == null ? null : vazioComoNulo(uf.toUpperCase(Locale.ROOT));
    }

    public String getCepFormatado() {
        if (cep == null || cep.length() != 8) {
            return cep;
        }
        return cep.substring(0, 5) + "-" + cep.substring(5);
    }

    /**
     * Ex.: "Rua das Flores, 123 - Apto 2 - Centro, Umuarama/PR - CEP 87501-000".
     * Devolve null se nada foi preenchido.
     */
    public String getCompleto() {
        List<String> partes = new ArrayList<>();

        if (logradouro != null) {
            partes.add(numero != null ? logradouro + ", " + numero : logradouro);
        }
        if (complemento != null) {
            partes.add(complemento);
        }
        if (bairro != null) {
            partes.add(bairro);
        }

        String texto = String.join(" - ", partes);

        if (cidade != null) {
            String local = uf != null ? cidade + "/" + uf : cidade;
            texto = texto.isEmpty() ? local : texto + ", " + local;
        }
        if (cep != null) {
            texto = texto.isEmpty() ? "CEP " + getCepFormatado() : texto + " - CEP " + getCepFormatado();
        }

        return texto.isEmpty() ? null : texto;
    }

    private static String vazioComoNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }
}