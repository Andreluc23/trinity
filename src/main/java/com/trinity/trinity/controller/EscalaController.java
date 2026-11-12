package com.trinity.trinity.controller;

import com.trinity.trinity.exception.RegraNegocioException;
import com.trinity.trinity.model.Escala;
import com.trinity.trinity.model.FuncaoMusical;
import com.trinity.trinity.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/escalas")
public class EscalaController {

    private final EscalaService escalaService;
    private final EscalaItemService escalaItemService;
    private final MembroService membroService;
    private final FuncaoMusicalService funcaoMusicalService;

    public EscalaController(
            EscalaService escalaService,
            EscalaItemService escalaItemService,
            MembroService membroService,
            FuncaoMusicalService funcaoMusicalService) {

        this.escalaService = escalaService;
        this.escalaItemService = escalaItemService;
        this.membroService = membroService;
        this.funcaoMusicalService = funcaoMusicalService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("escalas", escalaService.listarTodas());
        model.addAttribute("funcoes", funcaoMusicalService.listarTodas());
        model.addAttribute("novaFuncao", new FuncaoMusical());
        return "escalas";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        return exibirFormulario(new Escala(), List.of(), model);
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Long id, Model model) {
        List<IntegranteEscala> integrantes = escalaService.listarIntegrantes(id).stream()
                .map(item -> new IntegranteEscala(item.getMembro().getId(), item.getFuncaoMusical().getId()))
                .toList();

        return exibirFormulario(escalaService.buscarPorId(id), integrantes, model);
    }

    @PostMapping
    public String salvar(@ModelAttribute("escala") Escala escala,
                         @RequestParam(name = "membroIds", required = false) List<Long> membroIds,
                         @RequestParam(name = "funcaoIds", required = false) List<Long> funcaoIds,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        List<IntegranteEscala> integrantes = combinar(membroIds, funcaoIds);

        try {
            Escala escalaSalva = escalaService.salvar(escala, integrantes);
            redirectAttributes.addFlashAttribute("sucesso", "Escala salva com sucesso.");
            return "redirect:/escalas/" + escalaSalva.getId();
        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            return exibirFormulario(escala, integrantes, model);
        }
    }

    private String exibirFormulario(Escala escala, List<IntegranteEscala> integrantes, Model model) {
        model.addAttribute("escala", escala);
        model.addAttribute("integrantes", integrantes);
        model.addAttribute("membros", membroService.listarAtivos());
        model.addAttribute("funcoes", funcaoMusicalService.listarAtivas());
        return "forms/escala-form";
    }

    /**
     * O formulário envia duas listas paralelas: a 1ª posição de membroIds
     * forma par com a 1ª posição de funcaoIds, e assim por diante.
     */
    private List<IntegranteEscala> combinar(List<Long> membroIds, List<Long> funcaoIds) {
        if (membroIds == null || funcaoIds == null) {
            return List.of();
        }

        return IntStream.range(0, Math.min(membroIds.size(), funcaoIds.size()))
                .mapToObj(i -> new IntegranteEscala(membroIds.get(i), funcaoIds.get(i)))
                .toList();
    }
    @PostMapping("/desativar/{id}")
    public String desativar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        escalaService.desativar(id);
        redirectAttributes.addFlashAttribute("sucesso", "Escala desativada.");
        return "redirect:/escalas";
    }

    @PostMapping("/ativar/{id}")
    public String ativar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        escalaService.ativar(id);
        redirectAttributes.addFlashAttribute("sucesso", "Escala ativada.");
        return "redirect:/escalas";
    }
    @PostMapping("/{escalaId}/integrantes")
    public String adicionarIntegrante(
            @PathVariable("escalaId") Long escalaId,
            @RequestParam("membroId") Long membroId,
            @RequestParam("funcaoId") Long funcaoId) {

        var escala = escalaService.buscarPorId(escalaId);
        var membro = membroService.buscarPorId(membroId);
        var funcao = funcaoMusicalService.buscarPorId(funcaoId);

        escalaItemService.salvar(escala, membro, funcao);

        return "redirect:/escalas/" + escalaId;
    }
    @PostMapping("/{escalaId}/integrantes/remover/{itemId}")
    public String removerIntegrante(
            @PathVariable("escalaId") Long escalaId,
            @PathVariable("itemId") Long itemId) {

        escalaItemService.remover(itemId);

        return "redirect:/escalas/" + escalaId;
    }
    @GetMapping("/{id}")
    public String detalhes(@PathVariable("id") Long id, Model model) {

        model.addAttribute("escala", escalaService.buscarPorId(id));
        model.addAttribute("itens", escalaItemService.listarPorEscala(id));
        model.addAttribute("membros", membroService.listarTodos());
        model.addAttribute("funcoes", funcaoMusicalService.listarTodas());

        return "escala-detalhes";
    }
    @PostMapping("/{escalaId}/integrantes/editar/{itemId}")
    public String editarIntegrante(
            @PathVariable("escalaId") Long escalaId,
            @PathVariable("itemId") Long itemId,
            @RequestParam("membroId") Long membroId,
            @RequestParam("funcaoId") Long funcaoId) {

        var membro = membroService.buscarPorId(membroId);
        var funcao = funcaoMusicalService.buscarPorId(funcaoId);

        escalaItemService.atualizar(itemId, membro, funcao);

        return "redirect:/escalas/" + escalaId;
    }
    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        escalaService.excluir(id);
        redirectAttributes.addFlashAttribute("sucesso", "Escala excluída.");
        return "redirect:/escalas";
    }
}