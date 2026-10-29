package com.trinity.trinity.controller;

import com.trinity.trinity.model.CategoriaFinanceira;
import com.trinity.trinity.model.TipoLancamento;
import com.trinity.trinity.service.CategoriaFinanceiraService;
import com.trinity.trinity.service.LancamentoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

@Controller
@RequestMapping("/financeiro")
public class FinanceiroController {

    private final CategoriaFinanceiraService categoriaService;
    private final LancamentoService lancamentoService;

    public FinanceiroController(CategoriaFinanceiraService categoriaService,
                                LancamentoService lancamentoService) {
        this.categoriaService = categoriaService;
        this.lancamentoService = lancamentoService;
    }

    @GetMapping
    public String painel(@RequestParam(name = "mes", required = false) String mesInformado, Model model) {
        YearMonth mes = interpretarMes(mesInformado);

        BigDecimal entradas = lancamentoService.somarDoMes(TipoLancamento.ENTRADA, mes);
        BigDecimal saidas = lancamentoService.somarDoMes(TipoLancamento.SAIDA, mes);

        model.addAttribute("mes", mes);
        model.addAttribute("lancamentos", lancamentoService.listarDoMes(mes));
        model.addAttribute("entradas", entradas);
        model.addAttribute("saidas", saidas);
        model.addAttribute("saldo", entradas.subtract(saidas));

        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("novaCategoria", new CategoriaFinanceira());
        model.addAttribute("tipos", TipoLancamento.values());

        return "financeiro";
    }

    private YearMonth interpretarMes(String mesInformado) {
        if (mesInformado == null || mesInformado.isBlank()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(mesInformado);
        } catch (DateTimeParseException e) {
            return YearMonth.now();
        }
    }
}