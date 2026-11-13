package com.trinity.trinity.service;

import com.trinity.trinity.exception.RecursoNaoEncontradoException;
import com.trinity.trinity.exception.RegraNegocioException;
import com.trinity.trinity.model.Membro;
import com.trinity.trinity.model.Usuario;
import com.trinity.trinity.repository.ContribuicaoRepository;
import com.trinity.trinity.repository.EscalaItemRepository;
import com.trinity.trinity.repository.LancamentoRepository;
import com.trinity.trinity.repository.LeituraAvisoRepository;
import com.trinity.trinity.repository.MembroRepository;
import com.trinity.trinity.repository.PresencaEventoRepository;
import com.trinity.trinity.repository.RefreshTokenRepository;
import com.trinity.trinity.repository.UsuarioRepository;
import com.trinity.trinity.util.Cpf;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MembroService {

    private final MembroRepository membroRepository;
    private final UsuarioRepository usuarioRepository;
    private final LancamentoRepository lancamentoRepository;
    private final ContribuicaoRepository contribuicaoRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PresencaEventoRepository presencaEventoRepository;
    private final LeituraAvisoRepository leituraAvisoRepository;
    private final EscalaItemRepository escalaItemRepository;

    public MembroService(MembroRepository membroRepository,
                         UsuarioRepository usuarioRepository,
                         LancamentoRepository lancamentoRepository,
                         ContribuicaoRepository contribuicaoRepository,
                         RefreshTokenRepository refreshTokenRepository,
                         PresencaEventoRepository presencaEventoRepository,
                         LeituraAvisoRepository leituraAvisoRepository,
                         EscalaItemRepository escalaItemRepository) {
        this.membroRepository = membroRepository;
        this.usuarioRepository = usuarioRepository;
        this.lancamentoRepository = lancamentoRepository;
        this.contribuicaoRepository = contribuicaoRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.presencaEventoRepository = presencaEventoRepository;
        this.leituraAvisoRepository = leituraAvisoRepository;
        this.escalaItemRepository = escalaItemRepository;
    }

    public List<Membro> listarTodos() {
        return membroRepository.findAll();
    }

    public Membro salvar(Membro membro) {

        boolean cpfDuplicado;
        boolean emailDuplicado = false;
        if (!Cpf.isValido(membro.getCpfFormatado())) {
            throw new RegraNegocioException("CPF inválido.");
        }

        membro.setCpf(Cpf.somenteDigitos(membro.getCpfFormatado()));
        if (membro.getId() == null) {
            // Novo membro
            cpfDuplicado = membroRepository.existsByCpf(membro.getCpfFormatado());

            if (membro.getEmail() != null && !membro.getEmail().isBlank()) {
                emailDuplicado = membroRepository.existsByEmail(membro.getEmail());
            }

        } else {
            // Edição
            cpfDuplicado = membroRepository.existsByCpfAndIdNot(
                    membro.getCpfFormatado(),
                    membro.getId()
            );

            if (membro.getEmail() != null && !membro.getEmail().isBlank()) {
                emailDuplicado = membroRepository.existsByEmailAndIdNot(
                        membro.getEmail(),
                        membro.getId()
                );
            }
        }
        membro.getEndereco().normalizar();

        String cep = membro.getEndereco().getCep();
        if (cep != null && cep.length() != 8) {
            throw new RegraNegocioException("CEP inválido.");
        }

        if (cpfDuplicado) {
            throw new RegraNegocioException("CPF já cadastrado.");
        }

        if (emailDuplicado) {
            throw new RegraNegocioException("E-mail já cadastrado.");
        }
        if (membro.getDataNascimento() == null) {
            throw new RegraNegocioException("Informe a data de nascimento.");
        }

        if (membro.getDataNascimento().isAfter(LocalDate.now())) {
            throw new RegraNegocioException("A data de nascimento não pode ser no futuro.");
        }

        if (membro.getDataNascimento().getYear() < 1900) {
            throw new RegraNegocioException("Confira a data de nascimento: o ano informado é muito antigo.");
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

    /**
     * Exclui o membro e tudo que depende dele. Membros com histórico financeiro
     * não são excluídos (devem ser desativados). Tudo ou nada, numa única transação.
     */
    @Transactional
    public void excluir(Long id, String emailUsuarioLogado) {
        Membro membro = buscarPorId(id);

        if (lancamentoRepository.existsByMembroId(id) || contribuicaoRepository.existsByMembroId(id)) {
            throw new RegraNegocioException(membro.getNomeCompleto()
                    + " possui lançamentos ou contribuições registrados e não pode ser excluído. Use Desativar.");
        }

        Optional<Usuario> conta = usuarioRepository.findByMembroId(id);

        if (conta.isPresent()) {
            String emailMembro = membro.getEmail();

            if (emailMembro != null && emailMembro.equalsIgnoreCase(emailUsuarioLogado)) {
                throw new RegraNegocioException("Você não pode excluir o seu próprio cadastro.");
            }

            if (conta.get().isAdmin()) {
                throw new RegraNegocioException("Remova o acesso de administrador antes de excluir este membro.");
            }
        }

        conta.ifPresent(usuario -> {
            refreshTokenRepository.deleteByUsuarioId(usuario.getId());
            usuarioRepository.delete(usuario);
        });

        presencaEventoRepository.deleteByMembroId(id);
        leituraAvisoRepository.deleteByMembroId(id);
        escalaItemRepository.deleteByMembroId(id);

        membroRepository.delete(membro);
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

        String digitos = Cpf.somenteDigitos(termo);
        String termoCpf = digitos.isEmpty() ? termo : digitos;

        return membroRepository
                .findByNomeCompletoContainingIgnoreCaseOrCpfContainingOrTelefoneContaining(
                        termo,
                        termoCpf,
                        termo,
                        pageable
                );
    }

    public List<Membro> listarAtivos() {
        return membroRepository.findByAtivoTrueOrderByNomeCompletoAsc();
    }

    public List<Membro> listarTodosPorNome() {
        return membroRepository.findAllByOrderByNomeCompletoAsc();
    }
}