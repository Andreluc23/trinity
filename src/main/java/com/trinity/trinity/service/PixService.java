package com.trinity.trinity.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;

/**
 * Gera o código PIX "copia e cola" (BR Code estático) no padrão do Banco Central.
 */
@Service
public class PixService {

    private static final String GUI_PIX = "br.gov.bcb.pix";
    private static final int TAMANHO_MAXIMO_NOME = 25;
    private static final int TAMANHO_MAXIMO_CIDADE = 15;

    private final String chave;
    private final String nomeRecebedor;
    private final String cidade;

    public PixService(@Value("${trinity.pix.chave}") String chave,
                      @Value("${trinity.pix.nome-recebedor}") String nomeRecebedor,
                      @Value("${trinity.pix.cidade}") String cidade) {
        this.chave = chave.trim();
        this.nomeRecebedor = limitar(removerAcentos(nomeRecebedor), TAMANHO_MAXIMO_NOME);
        this.cidade = limitar(removerAcentos(cidade), TAMANHO_MAXIMO_CIDADE);
    }

    public String getNomeRecebedor() {
        return nomeRecebedor;
    }

    public String gerarCopiaECola(BigDecimal valor, String identificador) {
        String contaRecebedor = campo("00", GUI_PIX) + campo("01", chave);

        String payload = campo("00", "01")                     // versão do padrão
                + campo("26", contaRecebedor)                  // chave PIX
                + campo("52", "0000")                          // categoria do recebedor
                + campo("53", "986")                           // moeda: real
                + campo("54", formatarValor(valor))            // valor
                + campo("58", "BR")                            // país
                + campo("59", nomeRecebedor)                   // nome do recebedor
                + campo("60", cidade)                          // cidade
                + campo("62", campo("05", identificador))      // identificador da transação
                + "6304";                                      // início do CRC

        return payload + crc16(payload);
    }

    private static String campo(String id, String valor) {
        return id + String.format("%02d", valor.length()) + valor;
    }

    private static String formatarValor(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * CRC16-CCITT (polinômio 0x1021, valor inicial 0xFFFF), exigido pelo padrão PIX.
     */
    static String crc16(String texto) {
        int crc = 0xFFFF;

        for (byte b : texto.getBytes(StandardCharsets.UTF_8)) {
            crc ^= (b & 0xFF) << 8;

            for (int i = 0; i < 8; i++) {
                crc = (crc & 0x8000) != 0 ? (crc << 1) ^ 0x1021 : crc << 1;
                crc &= 0xFFFF;
            }
        }

        return String.format("%04X", crc);
    }

    private static String removerAcentos(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim();
    }

    private static String limitar(String texto, int tamanhoMaximo) {
        return texto.length() <= tamanhoMaximo ? texto : texto.substring(0, tamanhoMaximo);
    }
}