package com.trinity.trinity.util;

public final class Cpf {

    private Cpf() {
    }

    /** Remove tudo que não é número do texto. */
    public static String somenteDigitos(String texto) {
        return texto == null ? "" : texto.replaceAll("\\D", "");
    }

    /** Valida o CPF: 11 dígitos, não todos iguais, e os dois dígitos verificadores corretos. */
    public static boolean isValido(String cpf) {
        String digitos = somenteDigitos(cpf);

        if (digitos.length() != 11 || digitos.chars().distinct().count() == 1) {
            return false;
        }

        return calcularDigito(digitos, 9) == digitos.charAt(9) - '0'
                && calcularDigito(digitos, 10) == digitos.charAt(10) - '0';
    }

    /** Formata o CPF como 529.982.247-25; se não tiver 11 dígitos, devolve o texto original. */
    public static String formatar(String cpf) {
        String d = somenteDigitos(cpf);

        if (d.length() != 11) {
            return cpf;
        }

        return d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6, 9) + "-" + d.substring(9);
    }

    /**
     * Calcula um dígito verificador: cada um dos primeiros dígitos é multiplicado
     * por um peso decrescente, e o resto da divisão por 11 define o dígito.
     */
    private static int calcularDigito(String digitos, int quantidade) {
        int soma = 0;

        for (int i = 0; i < quantidade; i++) {
            soma += (digitos.charAt(i) - '0') * (quantidade + 1 - i);
        }

        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
    /**
     * Oculta o início e os dígitos verificadores: 529.982.247-25 vira ***.982.247-**.
     */
    public static String mascarar(String cpf) {
        String d = somenteDigitos(cpf);

        if (d.length() != 11) {
            return "***";
        }

        return "***." + d.substring(3, 6) + "." + d.substring(6, 9) + "-**";
    }
}