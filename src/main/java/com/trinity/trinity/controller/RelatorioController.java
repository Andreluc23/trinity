package com.trinity.trinity.controller;

import com.trinity.trinity.exception.RegraNegocioException;
import com.trinity.trinity.model.TipoLancamento;
import com.trinity.trinity.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;
    private final CategoriaFinanceiraService categoriaService;
    private final MembroService membroService;

    public RelatorioController(RelatorioService relatorioService,
                               CategoriaFinanceiraService categoriaService,
                               MembroService membroService) {
        this.relatorioService = relatorioService;
        this.categoriaService = categoriaService;
        this.membroService = membroService;
    }

    @GetMapping("/financeiro")
    public String financeiro(@ModelAttribute("filtro") FiltroRelatorioFinanceiro filtro, Model model) {
        model.addAttribute("tipos", TipoLancamento.values());
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("membros", membroService.listarTodosPorNome());
        model.addAttribute("filtrosAplicados", descreverFiltros(filtro));

        try {
            model.addAttribute("relatorio", relatorioService.gerarFinanceiro(filtro));
        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
        }

        return "relatorio-financeiro";
    }

    private List<String> descreverFiltros(FiltroRelatorioFinanceiro filtro) {
        List<String> descricoes = new ArrayList<>();

        if (filtro.tipo() != null) {
            descricoes.add("Tipo: " + filtro.tipo().getDescricao());
        }
        if (filtro.categoriaId() != null) {
            descricoes.add("Categoria: " + categoriaService.buscarPorId(filtro.categoriaId()).getNome());
        }
        if (filtro.membroId() != null) {
            descricoes.add("Membro: " + membroService.buscarPorId(filtro.membroId()).getNomeCompleto());
        }
        if (filtro.texto() != null && !filtro.texto().isBlank()) {
            descricoes.add("Descrição contém: " + filtro.texto().trim());
        }

        return descricoes;
    }
    @GetMapping("/membros")
    public String membros(@ModelAttribute("filtro") FiltroRelatorioMembros filtro, Model model) {
        model.addAttribute("relatorio", relatorioService.gerarMembros(filtro));
        return "relatorio-membros";
    }
}