package com.trinity.trinity.controller;

import com.trinity.trinity.service.BibliaService;
import com.trinity.trinity.service.LivroBiblia;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/biblia")
public class BibliaController {

    private final BibliaService bibliaService;

    public BibliaController(BibliaService bibliaService) {
        this.bibliaService = bibliaService;
    }

    @GetMapping
    public String livros(Model model) {
        List<LivroBiblia> livros = bibliaService.listarLivros();

        model.addAttribute("antigoTestamento", livros.stream().filter(l -> l.testamento().equals("AT")).toList());
        model.addAttribute("novoTestamento", livros.stream().filter(l -> l.testamento().equals("NT")).toList());
        return "biblia";
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
}