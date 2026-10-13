package com.trinity.trinity.security;

import com.trinity.trinity.model.Membro;
import com.trinity.trinity.model.Usuario;
import com.trinity.trinity.repository.MembroRepository;
import com.trinity.trinity.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
public class AdministradorInicial implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdministradorInicial.class);

    private final MembroRepository membroRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String senha;

    public AdministradorInicial(MembroRepository membroRepository,
                                UsuarioRepository usuarioRepository,
                                PasswordEncoder passwordEncoder,
                                @Value("${trinity.admin.email}") String email,
                                @Value("${trinity.admin.senha}") String senha) {
        this.membroRepository = membroRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.senha = senha;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.existsByAdminTrue()) {
            return;
        }

        Membro membro = membroRepository.findByEmailIgnoreCase(email)
                .orElseGet(this::criarMembro);
        membro.setAtivo(true);

        Usuario usuario = usuarioRepository.findByMembroId(membro.getId())
                .orElseGet(() -> novoUsuario(membro));
        usuario.setAtivo(true);
        usuario.setAdmin(true);
        usuarioRepository.save(usuario);

        log.warn("Administrador inicial configurado: {}", email);
    }

    private Membro criarMembro() {
        Membro membro = new Membro();
        membro.setNomeCompleto("Administrador do Sistema");
        membro.setEmail(email);
        membro.setCpf("00000000000");
        membro.setTelefone("(00) 00000-0000");
        membro.setDataNascimento(LocalDate.of(2000, 1, 1));
        return membroRepository.save(membro);
    }

    private Usuario novoUsuario(Membro membro) {
        Usuario usuario = new Usuario();
        usuario.setMembro(membro);
        usuario.setSenha(passwordEncoder.encode(senha));
        return usuario;
    }
}