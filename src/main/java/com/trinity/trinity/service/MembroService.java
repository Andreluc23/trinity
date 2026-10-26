package com.trinity.trinity.service;

import com.trinity.trinity.exception.RecursoNaoEncontradoException;
import com.trinity.trinity.exception.RegraNegocioException;
import com.trinity.trinity.model.Membro;
import com.trinity.trinity.repository.MembroRepository;
import com.trinity.trinity.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
public class MembroService {

    private final MembroRepository membroRepository;
    private final UsuarioRepository usuarioRepository;

    public MembroService(MembroRepository membroRepository, UsuarioRepository usuarioRepository) {
        this.membroRepository = membroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Membro> listarTodos() {
        return membroRepository.findAll();
    }

    public Membro salvar(Membro membro) {

        boolean cpfDuplicado;
        boolean emailDuplicado = false;

        if (membro.getId() == null) {
            // Novo membro
            cpfDuplicado = membroRepository.existsByCpf(membro.getCpf());

            if (membro.getEmail() != null && !membro.getEmail().isBlank()) {
                emailDuplicado = membroRepository.existsByEmail(membro.getEmail());
            }

        } else {
            // Edição
            cpfDuplicado = membroRepository.existsByCpfAndIdNot(
                    membro.getCpf(),
                    membro.getId()
            );

            if (membro.getEmail() != null && !membro.getEmail().isBlank()) {
                emailDuplicado = membroRepository.existsByEmailAndIdNot(
                        membro.getEmail(),
                        membro.getId()
                );
            }
        }

        if (cpfDuplicado) {
            throw new RegraNegocioException("CPF já cadastrado.");
        }

        if (emailDuplicado) {
            throw new RegraNegocioException("E-mail já cadastrado.");
        }

        return membroRepository.save(membro);
    }

    public Membro buscarPorId(Long id) {
        return membroRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Membro não encontrado"));
    }

    public void desativar(Long id) {
        Membro membro = buscarPorId(id);

        if (ehUnicoAdministradorAtivo(membro)) {
            throw new RegraNegocioException("Não é possível desativar o único administrador ativo do sistema.");
        }

        membro.setAtivo(false);
        membroRepository.save(membro);
    }

    private boolean ehUnicoAdministradorAtivo(Membro membro) {
        boolean ehAdministrador = usuarioRepository.findByMembroId(membro.getId())
                .filter(usuario -> usuario.isAdmin() && usuario.isAtivo())
                .isPresent();

        return ehAdministrador
                && usuarioRepository.countByAdminTrueAndAtivoTrueAndMembro_AtivoTrue() <= 1;
    }
    public void ativar(Long id) {

        Membro membro = buscarPorId(id);

        membro.setAtivo(true);

        membroRepository.save(membro);
    }

    public long contarTodos() {
        return membroRepository.count();
    }

    public long contarAtivos() {
        return membroRepository.countByAtivo(true);
    }

    public long contarInativos() {
        return membroRepository.countByAtivo(false);
    }

    public Page<Membro> buscar(String termo, int pagina) {

        Pageable pageable = PageRequest.of(pagina, 5);

        if (termo == null || termo.isBlank()) {
            return membroRepository.findAll(pageable);
        }

        return membroRepository
                .findByNomeCompletoContainingIgnoreCaseOrCpfContainingOrTelefoneContaining(
                        termo,
                        termo,
                        termo,
                        pageable
                );
    }
}