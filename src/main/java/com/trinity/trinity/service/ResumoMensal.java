package com.trinity.trinity.service;

import java.math.BigDecimal;
import java.time.YearMonth;

public record ResumoMensal(YearMonth mes, BigDecimal entradas, BigDecimal saidas) {
}