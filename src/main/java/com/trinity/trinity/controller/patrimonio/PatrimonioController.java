package com.trinity.trinity.controller.patrimonio;


import com.trinity.trinity.model.patrimonio.Patrimonio;
import com.trinity.trinity.service.patrimonio.PatrimonioService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/patrimonios")
public class PatrimonioController {

    private static final String REDIRECT_LISTA = "redirect:/patrimonios";

    private final PatrimonioService patrimonioService;

    public PatrimonioController(PatrimonioService patrimonioService) {
        this.patrimonioService = patrimonioService;
    }

    /** Lista os itens do patrimônio com busca por nome, categoria ou localização, e paginação. */
    @GetMapping
    public String listar(@RequestParam(name = "termo", required = false) String termo,
                         @RequestParam(name = "pagina", defaultValue = "0") int pagina,
                         Model model) {
        Page<Patrimonio> paginaPatrimonios = patrimonioService.buscar(termo, pagina);

        model.addAttribute("patrimonios", paginaPatrimonios.getContent());
        model.addAttribute("paginaAtual", pagina);
        model.addAttribute("totalPaginas", paginaPatrimonios.getTotalPages());
        model.addAttribute("termo", termo);
        return "patrimonios";
    }

    /** Abre o formulário para cadastrar um item. */
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("patrimonio", new Patrimonio());
        return "forms/patrimonio-form";
    }

    /** Abre o formulário preenchido para editar um item. */
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Long id, Model model) {
        model.addAttribute("patrimonio", patrimonioService.buscarPorId(id));
        return "forms/patrimonio-form";
    }

    /** Grava o item (novo ou editado) e volta para a lista. */
    @PostMapping
    public String salvar(Patrimonio patrimonio, RedirectAttributes redirectAttributes) {
        patrimonioService.salvar(patrimonio);
        redirectAttributes.addFlashAttribute("sucesso", "Patrimônio salvo com sucesso.");
        return REDIRECT_LISTA;
    }

    /** Desativa o item. */
    @PostMapping("/desativar/{id}")
    public String desativar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        patrimonioService.desativar(id);
        redirectAttributes.addFlashAttribute("sucesso", "Patrimônio desativado.");
        return REDIRECT_LISTA;
    }

    /** Reativa o item. */
    @PostMapping("/ativar/{id}")
    public String ativar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        patrimonioService.ativar(id);
        redirectAttributes.addFlashAttribute("sucesso", "Patrimônio ativado.");
        return REDIRECT_LISTA;
    }
    /** Exclui o item. */
    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        patrimonioService.excluir(id);
        redirectAttributes.addFlashAttribute("sucesso", "Patrimônio excluído.");
        return REDIRECT_LISTA;
    }
}