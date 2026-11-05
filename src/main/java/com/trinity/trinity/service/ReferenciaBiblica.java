package com.trinity.trinity.service;

/**
 * Aponta para um capítulo: usado nos botões "anterior" e "próximo".
 */
public record ReferenciaBiblica(LivroBiblia livro, int capitulo) {
}