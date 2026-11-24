package com.trinity.trinity.controller.financeiro;

import com.trinity.trinity.exception.RegraNegocioException;
import com.trinity.trinity.model.financeiro.CategoriaFinanceira;
import com.trinity.trinity.model.financeiro.TipoLancamento;
import com.trinity.trinity.service.financeiro.CategoriaFinanceiraService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/financeiro/categorias")
public class CategoriaFinanceiraController {

    private static final String FORMULARIO = "forms/categoria-financeira-form";

    private final CategoriaFinanceiraService categoriaService;

    public CategoriaFinanceiraController(CategoriaFinanceiraService categoriaService) {
        this.categoriaService = categoriaService;
    }

    /** Disponibiliza os tipos de lançamento (entrada e saída) para os formulários desta tela. */
    @ModelAttribute("tipos")
    public TipoLancamento[] tipos() {
        return TipoLancamento.values();
    }

    /** Abre o formulário de edição de uma categoria financeira. */
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Long id, Model model) {
        if (!model.containsAttribute("categoria")) {
            model.addAttribute("categoria", categoriaService.buscarPorId(id));
        }
        return FORMULARIO;
    }

    /** Salva a categoria; em erro, uma nova volta ao modal e uma edição volta ao formulário. */
    @PostMapping
    public String salvar(@ModelAttribute("categoria") CategoriaFinanceira categoria,
                         RedirectAttributes redirectAttributes) {
        try {
            categoriaService.salvar(categoria);
            redirectAttributes.addFlashAttribute("sucessoCategoria", "Categoria salva com sucesso.");
        } catch (RegraNegocioException e) {
            if (categoria.getId() != null) {
                redirectAttributes.addFlashAttribute("erro", e.getMessage());
                redirectAttributes.addFlashAttribute("categoria", categoria);
                return "redirect:/financeiro/categorias/editar/" + categoria.getId();
            }
            redirectAttributes.addFlashAttribute("erroCategoria", e.getMessage());
        }
        return voltarComModalAberto(redirectAttributes);
    }

    /** Desativa a categoria (ela deixa de aparecer para novos lançamentos). */
    @PostMapping("/desativar/{id}")
    public String desativar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        categoriaService.desativar(id);
        return voltarComModalAberto(redirectAttributes);
    }

    /** Reativa a categoria. */
    @PostMapping("/ativar/{id}")
    public String ativar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        categoriaService.ativar(id);
        return voltarComModalAberto(redirectAttributes);
    }

    /** Exclui a categoria, desde que nunca tenha sido usada. */
    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            categoriaService.excluir(id);
            redirectAttributes.addFlashAttribute("sucessoCategoria", "Categoria excluída.");
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erroCategoria", e.getMessage());
        }
        return voltarComModalAberto(redirectAttributes);
    }

    /** Volta ao painel financeiro com o modal de categorias já aberto. */
    private String voltarComModalAberto(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("abrirCategorias", true);
        return "redirect:/financeiro";
    }

}