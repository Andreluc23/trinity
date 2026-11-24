package com.trinity.trinity.controller.escalas;

import com.trinity.trinity.exception.RegraNegocioException;

import com.trinity.trinity.model.escalas.FuncaoMusical;
import com.trinity.trinity.service.escalas.FuncaoMusicalService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/funcoes-musicais")
public class FuncaoMusicalController {

    private static final String FORMULARIO = "forms/funcao-musical-form";

    private final FuncaoMusicalService funcaoMusicalService;

    public FuncaoMusicalController(FuncaoMusicalService funcaoMusicalService) {
        this.funcaoMusicalService = funcaoMusicalService;
    }

    /** Abre o formulário de edição de uma função musical. */
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Long id, Model model) {
        model.addAttribute("funcaoMusical", funcaoMusicalService.buscarPorId(id));
        return FORMULARIO;
    }

    /** Salva a função; em erro, uma nova volta ao modal de funções e uma edição volta ao formulário. */
    @PostMapping
    public String salvar(@ModelAttribute("funcaoMusical") FuncaoMusical funcaoMusical,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        try {
            funcaoMusicalService.salvar(funcaoMusical);
            redirectAttributes.addFlashAttribute("sucessoFuncao", "Função salva com sucesso.");
        } catch (RegraNegocioException e) {
            if (funcaoMusical.getId() != null) {
                model.addAttribute("erro", e.getMessage());
                return FORMULARIO;
            }
            redirectAttributes.addFlashAttribute("erroFuncao", e.getMessage());
        }
        return voltarComModalAberto(redirectAttributes);
    }

    /** Desativa a função musical. */
    @PostMapping("/desativar/{id}")
    public String desativar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        funcaoMusicalService.desativar(id);
        return voltarComModalAberto(redirectAttributes);
    }

    /** Reativa a função musical. */
    @PostMapping("/ativar/{id}")
    public String ativar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        funcaoMusicalService.ativar(id);
        return voltarComModalAberto(redirectAttributes);
    }
    /** Exclui a função, desde que nunca tenha sido usada em uma escala. */
    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            funcaoMusicalService.excluir(id);
            redirectAttributes.addFlashAttribute("sucessoFuncao", "Função excluída.");
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erroFuncao", e.getMessage());
        }
        return voltarComModalAberto(redirectAttributes);
    }

    /** Volta para a tela de escalas com o modal de funções musicais já aberto. */
    private String voltarComModalAberto(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("abrirFuncoes", true);
        return "redirect:/escalas";
    }
    /** Cria uma função musical pelo formulário de escala (resposta JSON, sem recarregar a página). */
    @PostMapping("/rapida")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> criarRapida(@RequestParam(name = "nome", required = false) String nome) {
        try {
            FuncaoMusical funcao = new FuncaoMusical();
            funcao.setNome(nome);

            FuncaoMusical salva = funcaoMusicalService.salvar(funcao);
            return ResponseEntity.ok(Map.of("id", salva.getId(), "nome", salva.getNome()));
        } catch (RegraNegocioException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}