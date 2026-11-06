package com.trinity.trinity.controller;

import com.trinity.trinity.exception.RegraNegocioException;
import com.trinity.trinity.service.BibliaService;
import com.trinity.trinity.service.LivroBiblia;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/biblia")
public class BibliaController {

    private final BibliaService bibliaService;

    public BibliaController(BibliaService bibliaService) {
        this.bibliaService = bibliaService;
    }

    @GetMapping
    public String livros(Model model) {
        model.addAttribute("antigoTestamento", agruparPorCategoria("AT"));
        model.addAttribute("novoTestamento", agruparPorCategoria("NT"));
        return "biblia";
    }

    private Map<String, List<LivroBiblia>> agruparPorCategoria(String testamento) {
        return bibliaService.listarLivros().stream()
                .filter(livro -> livro.testamento().equals(testamento))
                .collect(Collectors.groupingBy(LivroBiblia::categoria, LinkedHashMap::new, Collectors.toList()));
    }

    @GetMapping("/{livro}")
    public String capitulos(@PathVariable("livro") int livro, Model model) {
        model.addAttribute("livro", bibliaService.buscarLivro(livro));
        return "biblia-livro";
    }

    @GetMapping("/{livro}/{capitulo}")
    public String leitura(@PathVariable("livro") int livro,
                          @PathVariable("capitulo") int capitulo,
                          Model model) {
        model.addAttribute("livro", bibliaService.buscarLivro(livro));
        model.addAttribute("capitulo", capitulo);
        model.addAttribute("versiculos", bibliaService.buscarCapitulo(livro, capitulo));
        model.addAttribute("anterior", bibliaService.capituloAnterior(livro, capitulo));
        model.addAttribute("proximo", bibliaService.proximoCapitulo(livro, capitulo));
        return "biblia-capitulo";
    }
    @GetMapping("/busca")
    public String busca(@RequestParam(name = "termo", required = false) String termo, Model model) {
        model.addAttribute("termo", termo);

        try {
            model.addAttribute("resultado", bibliaService.buscar(termo));
        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
        }

        return "biblia-busca";
    }
}