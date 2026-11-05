package com.trinity.trinity.controller;

import com.trinity.trinity.exception.RegraNegocioException;
import com.trinity.trinity.model.CategoriaFinanceira;
import com.trinity.trinity.model.TipoLancamento;
import com.trinity.trinity.service.CategoriaFinanceiraService;
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

    @ModelAttribute("tipos")
    public TipoLancamento[] tipos() {
        return TipoLancamento.values();
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Long id, Model model) {
        model.addAttribute("categoria", categoriaService.buscarPorId(id));
        return FORMULARIO;
    }

    @PostMapping
    public String salvar(@ModelAttribute("categoria") CategoriaFinanceira categoria,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        try {
            categoriaService.salvar(categoria);
            redirectAttributes.addFlashAttribute("sucessoCategoria", "Categoria salva com sucesso.");
        } catch (RegraNegocioException e) {
            if (categoria.getId() != null) {
                model.addAttribute("erro", e.getMessage());
                return FORMULARIO;
            }
            redirectAttributes.addFlashAttribute("erroCategoria", e.getMessage());
        }
        return voltarComModalAberto(redirectAttributes);
    }

    @PostMapping("/desativar/{id}")
    public String desativar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        categoriaService.desativar(id);
        return voltarComModalAberto(redirectAttributes);
    }

    @PostMapping("/ativar/{id}")
    public String ativar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        categoriaService.ativar(id);
        return voltarComModalAberto(redirectAttributes);
    }

    private String voltarComModalAberto(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("abrirCategorias", true);
        return "redirect:/financeiro";
    }
}