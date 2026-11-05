package com.trinity.trinity.controller;

import com.trinity.trinity.exception.RegraNegocioException;
import com.trinity.trinity.model.Lancamento;
import com.trinity.trinity.model.TipoLancamento;
import com.trinity.trinity.service.CategoriaFinanceiraService;
import com.trinity.trinity.service.LancamentoService;
import com.trinity.trinity.service.MembroService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.YearMonth;

@Controller
@RequestMapping("/financeiro/lancamentos")
public class LancamentoController {

    private static final String FORMULARIO = "forms/lancamento-form";

    private final LancamentoService lancamentoService;
    private final CategoriaFinanceiraService categoriaService;
    private final MembroService membroService;

    public LancamentoController(LancamentoService lancamentoService,
                                CategoriaFinanceiraService categoriaService,
                                MembroService membroService) {
        this.lancamentoService = lancamentoService;
        this.categoriaService = categoriaService;
        this.membroService = membroService;
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        Lancamento lancamento = new Lancamento();
        lancamento.setData(LocalDate.now());
        return exibirFormulario(lancamento, model);
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Long id, Model model) {
        return exibirFormulario(lancamentoService.buscarPorId(id), model);
    }

    @PostMapping
    public String salvar(@ModelAttribute("lancamento") Lancamento lancamento,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        try {
            lancamentoService.salvar(lancamento);
            redirectAttributes.addFlashAttribute("sucesso", "Lançamento salvo com sucesso.");
            return voltarParaOMes(lancamento.getData());
        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            return exibirFormulario(lancamento, model);
        }
    }

    @PostMapping("/inativar/{id}")
    public String inativar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Lancamento lancamento = lancamentoService.buscarPorId(id);
        lancamentoService.inativar(id);
        redirectAttributes.addFlashAttribute("sucesso", "Lançamento inativado.");
        return voltarParaOMes(lancamento.getData());
    }

    private String exibirFormulario(Lancamento lancamento, Model model) {
        model.addAttribute("lancamento", lancamento);
        model.addAttribute("categoriasEntrada", categoriaService.listarAtivasPorTipo(TipoLancamento.ENTRADA));
        model.addAttribute("categoriasSaida", categoriaService.listarAtivasPorTipo(TipoLancamento.SAIDA));
        model.addAttribute("membros", membroService.listarAtivos());
        model.addAttribute("hoje", LocalDate.now());
        return FORMULARIO;
    }

    private String voltarParaOMes(LocalDate data) {
        return "redirect:/financeiro?mes=" + YearMonth.from(data);
    }
}