package com.trinity.trinity.controller.financeiro;

import com.trinity.trinity.exception.RegraNegocioException;
import com.trinity.trinity.model.financeiro.Lancamento;
import com.trinity.trinity.model.financeiro.TipoLancamento;
import com.trinity.trinity.service.financeiro.CategoriaFinanceiraService;
import com.trinity.trinity.service.financeiro.LancamentoService;
import com.trinity.trinity.service.membros.MembroService;
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

    /** Abre o formulário de novo lançamento, com a data de hoje. */
    @GetMapping("/novo")
    public String novo(Model model) {
        if (!model.containsAttribute("lancamento")) {
            Lancamento lancamento = new Lancamento();
            lancamento.setData(LocalDate.now());
            model.addAttribute("lancamento", lancamento);
        }
        return exibirFormulario(model);
    }

    /** Abre o formulário de edição de um lançamento. */
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Long id, Model model) {
        if (!model.containsAttribute("lancamento")) {
            model.addAttribute("lancamento", lancamentoService.buscarPorId(id));
        }
        return exibirFormulario(model);
    }

    /** Grava o lançamento; em erro de regra, volta ao formulário com o que foi digitado. */
    @PostMapping
    public String salvar(@ModelAttribute("lancamento") Lancamento lancamento,
                         RedirectAttributes redirectAttributes) {
        try {
            lancamentoService.salvar(lancamento);
            redirectAttributes.addFlashAttribute("sucesso", "Lançamento salvo com sucesso.");
            return voltarParaOMes(lancamento.getData());
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            redirectAttributes.addFlashAttribute("lancamento", lancamento);

            return lancamento.getId() == null
                    ? "redirect:/financeiro/lancamentos/novo"
                    : "redirect:/financeiro/lancamentos/editar/" + lancamento.getId();
        }
    }

    /** Inativa o lançamento (ele deixa de contar no saldo, mas continua registrado). */
    @PostMapping("/inativar/{id}")
    public String inativar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Lancamento lancamento = lancamentoService.buscarPorId(id);
        lancamentoService.inativar(id);
        redirectAttributes.addFlashAttribute("sucesso", "Lançamento inativado.");
        return voltarParaOMes(lancamento.getData());
    }
    /** Exclui o lançamento manual; lançamentos de PIX não podem ser excluídos. */
    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Lancamento lancamento = lancamentoService.buscarPorId(id);

        try {
            lancamentoService.excluir(id);
            redirectAttributes.addFlashAttribute("sucesso", "Lançamento excluído.");
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("falha", e.getMessage());
        }

        return voltarParaOMes(lancamento.getData());
    }

    /** Carrega categorias, membros e a data de hoje e abre o formulário de lançamento. */
    private String exibirFormulario(Model model) {
        model.addAttribute("categoriasEntrada", categoriaService.listarAtivasPorTipo(TipoLancamento.ENTRADA));
        model.addAttribute("categoriasSaida", categoriaService.listarAtivasPorTipo(TipoLancamento.SAIDA));
        model.addAttribute("membros", membroService.listarAtivos());
        model.addAttribute("hoje", LocalDate.now());
        return FORMULARIO;
    }

    /** Volta ao painel financeiro no mês da data informada. */
    private String voltarParaOMes(LocalDate data) {
        return "redirect:/financeiro?mes=" + YearMonth.from(data);
    }
}