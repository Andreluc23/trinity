package com.trinity.trinity.controller.escalas;

import com.trinity.trinity.exception.RegraNegocioException;

import com.trinity.trinity.model.escalas.Escala;
import com.trinity.trinity.model.escalas.FuncaoMusical;

import com.trinity.trinity.service.escalas.EscalaService;
import com.trinity.trinity.service.escalas.FuncaoMusicalService;
import com.trinity.trinity.service.escalas.IntegranteEscala;
import com.trinity.trinity.service.membros.MembroService;
import org.springframework.data.domain.Page;
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
    private final MembroService membroService;
    private final FuncaoMusicalService funcaoMusicalService;

    public EscalaController(EscalaService escalaService,
                            MembroService membroService,
                            FuncaoMusicalService funcaoMusicalService) {
        this.escalaService = escalaService;
        this.membroService = membroService;
        this.funcaoMusicalService = funcaoMusicalService;
    }

    /** Lista as escalas com paginação (próximas primeiro) e as funções musicais para o modal de funções. */
    @GetMapping
    public String listar(@RequestParam(name = "pagina", defaultValue = "0") int pagina, Model model) {
        Page<Escala> paginaEscalas = escalaService.listarTodas(pagina);

        model.addAttribute("escalas", paginaEscalas.getContent());
        model.addAttribute("paginaAtual", pagina);
        model.addAttribute("totalPaginas", paginaEscalas.getTotalPages());
        model.addAttribute("funcoes", funcaoMusicalService.listarTodas());
        model.addAttribute("novaFuncao", new FuncaoMusical());
        return "escalas";
    }
    /** Mostra os dados de uma escala e os seus integrantes (somente leitura). */
    @GetMapping("/{id}")
    public String detalhes(@PathVariable("id") Long id, Model model) {
        model.addAttribute("escala", escalaService.buscarPorId(id));
        model.addAttribute("itens", escalaService.listarIntegrantes(id));
        return "escala-detalhes";
    }

    /** Abre o formulário de nova escala; se voltou de um erro, mantém o que foi digitado. */
    @GetMapping("/nova")
    public String nova(Model model) {
        // Se voltou de um erro, a escala e os integrantes já vieram pelo redirecionamento
        if (!model.containsAttribute("escala")) {
            model.addAttribute("escala", new Escala());
            model.addAttribute("integrantes", List.of());
        }
        return exibirFormulario(model);
    }

    /** Abre o formulário de edição com os integrantes atuais; se voltou de um erro, mantém o que foi digitado. */
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Long id, Model model) {
        // Se voltou de um erro, mantém o que o usuário digitou em vez de buscar no banco
        if (!model.containsAttribute("escala")) {
            List<IntegranteEscala> integrantes = escalaService.listarIntegrantes(id).stream()
                    .map(item -> new IntegranteEscala(item.getMembro().getId(), item.getFuncaoMusical().getId()))
                    .toList();

            model.addAttribute("escala", escalaService.buscarPorId(id));
            model.addAttribute("integrantes", integrantes);
        }
        return exibirFormulario(model);
    }

    /** Grava a escala com os integrantes; em caso de erro de regra, volta ao formulário com o que foi digitado. */
    @PostMapping
    public String salvar(@ModelAttribute("escala") Escala escala,
                         @RequestParam(name = "membroIds", required = false) List<Long> membroIds,
                         @RequestParam(name = "funcaoIds", required = false) List<Long> funcaoIds,
                         RedirectAttributes redirectAttributes) {

        List<IntegranteEscala> integrantes = combinar(membroIds, funcaoIds);

        try {
            Escala escalaSalva = escalaService.salvar(escala, integrantes);
            redirectAttributes.addFlashAttribute("sucesso", "Escala salva com sucesso.");
            return "redirect:/escalas/" + escalaSalva.getId();
        } catch (RegraNegocioException e) {
            // Volta ao formulário por redirecionamento, levando o que foi digitado e o erro
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            redirectAttributes.addFlashAttribute("escala", escala);
            redirectAttributes.addFlashAttribute("integrantes", integrantes);

            return escala.getId() == null
                    ? "redirect:/escalas/nova"
                    : "redirect:/escalas/editar/" + escala.getId();
        }
    }

    /** Desativa a escala. */
    @PostMapping("/desativar/{id}")
    public String desativar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        escalaService.desativar(id);
        redirectAttributes.addFlashAttribute("sucesso", "Escala desativada.");
        return "redirect:/escalas";
    }

    /** Reativa a escala. */
    @PostMapping("/ativar/{id}")
    public String ativar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        escalaService.ativar(id);
        redirectAttributes.addFlashAttribute("sucesso", "Escala ativada.");
        return "redirect:/escalas";
    }

    /** Exclui a escala e os seus integrantes. */
    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        escalaService.excluir(id);
        redirectAttributes.addFlashAttribute("sucesso", "Escala excluída.");
        return "redirect:/escalas";
    }

    /** Carrega os membros ativos e as funções ativas e abre o formulário de escala. */
    private String exibirFormulario(Model model) {
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
}