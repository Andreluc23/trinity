package com.trinity.trinity.security;

import com.trinity.trinity.model.Usuario;
import com.trinity.trinity.repository.UsuarioRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        Usuario usuario = usuarioRepository.findByMembro_EmailIgnoreCase(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        boolean ativo = usuario.isAtivo() && usuario.getMembro().isAtivo();

        return User.withUsername(usuario.getMembro().getEmail())
                .password(usuario.getSenha())
                .authorities(permissoes(usuario))
                .disabled(!ativo)
                .build();
    }

    private List<GrantedAuthority> permissoes(Usuario usuario) {
        List<GrantedAuthority> lista = new ArrayList<>();
        lista.add(new SimpleGrantedAuthority("ROLE_MEMBRO"));

        adicionarSe(lista, usuario.isAdmin(), "ROLE_ADMIN");
        adicionarSe(lista, usuario.isGerenciarMembros(), "GERENCIAR_MEMBROS");
        adicionarSe(lista, usuario.isGerenciarAvisos(), "GERENCIAR_AVISOS");
        adicionarSe(lista, usuario.isGerenciarEventos(), "GERENCIAR_EVENTOS");
        adicionarSe(lista, usuario.isAcessoEscalas(), "VISUALIZAR_ESCALAS");
        adicionarSe(lista, usuario.isGerenciarEscalas(), "GERENCIAR_ESCALAS");
        adicionarSe(lista, usuario.isGerenciarPatrimonio(), "GERENCIAR_PATRIMONIO");
        adicionarSe(lista, usuario.isGerenciarFinanceiro(), "GERENCIAR_FINANCEIRO");

        return lista;
    }

    private void adicionarSe(List<GrantedAuthority> lista, boolean condicao, String permissao) {
        if (condicao) {
            lista.add(new SimpleGrantedAuthority(permissao));
        }
    }
}