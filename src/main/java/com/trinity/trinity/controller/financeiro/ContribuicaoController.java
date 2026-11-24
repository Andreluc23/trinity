package com.trinity.trinity.controller.financeiro;

import com.trinity.trinity.exception.RegraNegocioException;
import com.trinity.trinity.model.financeiro.Contribuicao;
import com.trinity.trinity.model.financeiro.TipoLancamento;
import com.trinity.trinity.service.financeiro.CategoriaFinanceiraService;
import com.trinity.trinity.service.financeiro.ContribuicaoService;
import com.trinity.trinity.service.financeiro.PixService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/contribuicoes")
public class ContribuicaoController {

    private final ContribuicaoService contribuicaoService;
    private final CategoriaFinanceiraService categoriaService;
    private final PixService pixService;

    public ContribuicaoController(ContribuicaoService contribuicaoService,
                                  CategoriaFinanceiraService categoriaService,
                                  PixService pixService) {
        this.contribuicaoService = contribuicaoService;
        this.categoriaService = categoriaService;
        this.pixService = pixService;
    }

    /** Tela de contribuição do membro: formulário de valor e tipo, e o histórico das suas contribuições. */
    @GetMapping
    public String inicio(Authentication authentication, Model model) {
        model.addAttribute("categorias", categoriaService.listarAtivasPorTipo(TipoLancamento.ENTRADA));
        model.addAttribute("contribuicoes", contribuicaoService.listarDoMembro(authentication.getName()));
        return "contribuicoes";
    }

    /** Cria uma contribuição (aguardando pagamento) e leva o membro à tela do PIX. */
    @PostMapping
    public String gerar(Authentication authentication,
                        @RequestParam(name = "valor", required = false) BigDecimal valor,
                        @RequestParam(name = "categoriaId", required = false) Long categoriaId,
                        RedirectAttributes redirectAttributes) {
        try {
            Contribuicao contribuicao = contribuicaoService.gerar(authentication.getName(), valor, categoriaId);
            return "redirect:/contribuicoes/" + contribuicao.getId();
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/contribuicoes";
        }
    }

    /** Mostra o QR Code e o código "copia e cola" do PIX da contribuição, só para o dono dela. */
    @GetMapping("/{id}")
    public String exibirPix(@PathVariable("id") Long id, Authentication authentication, Model model) {
        Contribuicao contribuicao = contribuicaoService.buscarDoMembro(id, authentication.getName());

        model.addAttribute("contribuicao", contribuicao);
        model.addAttribute("recebedor", pixService.getNomeRecebedor());
        model.addAttribute("copiaECola",
                pixService.gerarCopiaECola(contribuicao.getValor(), contribuicao.getIdentificador()));

        return "contribuicao-pix";
    }

    /** Registra que o membro informou ter pago, para a tesouraria confirmar. */
    @PostMapping("/{id}/paguei")
    public String informarPagamento(@PathVariable("id") Long id,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            contribuicaoService.informarPagamento(id, authentication.getName());
            redirectAttributes.addFlashAttribute("sucesso",
                    "Obrigado! A tesouraria vai confirmar o recebimento em breve.");
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/contribuicoes";
    }
}