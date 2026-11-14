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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MembroService {

    private static final int MEMBROS_POR_PAGINA = 5;

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

    // ---------- Consultas ----------

    public Membro buscarPorId(Long id) {
        return membroRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Membro não encontrado"));
    }

    public List<Membro> listarTodos() {
        return membroRepository.findAll();
    }

    public List<Membro> listarTodosPorNome() {
        return membroRepository.findAllByOrderByNomeCompletoAsc();
    }

    public List<Membro> listarAtivos() {
        return membroRepository.findByAtivoTrueOrderByNomeCompletoAsc();
    }

    public Page<Membro> buscar(String termo, int pagina) {
        Pageable pageable = PageRequest.of(pagina, MEMBROS_POR_PAGINA);

        if (termo == null || termo.isBlank()) {
            return membroRepository.findAll(pageable);
        }

        // A busca por CPF funciona com ou sem pontuação, porque o CPF é guardado só com números
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

    public long contarTodos() {
        return membroRepository.count();
    }

    public long contarAtivos() {
        return membroRepository.countByAtivo(true);
    }

    public long contarInativos() {
        return membroRepository.countByAtivo(false);
    }

    // ---------- Cadastro ----------

    public Membro salvar(Membro membro) {

        // 1. Valida e padroniza os dados
        if (!Cpf.isValido(membro.getCpf())) {
            throw new RegraNegocioException("CPF inválido.");
        }
        membro.setCpf(Cpf.somenteDigitos(membro.getCpf()));

        if (membro.getDataNascimento() == null) {
            throw new RegraNegocioException("Informe a data de nascimento.");
        }
        if (membro.getDataNascimento().isAfter(LocalDate.now())) {
            throw new RegraNegocioException("A data de nascimento não pode ser no futuro.");
        }
        if (membro.getDataNascimento().getYear() < 1900) {
            throw new RegraNegocioException("Confira a data de nascimento: o ano informado é muito antigo.");
        }

        membro.getEndereco().normalizar();
        String cep = membro.getEndereco().getCep();
        if (cep != null && cep.length() != 8) {
            throw new RegraNegocioException("CEP inválido.");
        }

        // 2. Verifica duplicidades (o CPF já está só com números, como no banco)
        boolean novo = membro.getId() == null;
        String cpf = membro.getCpf();
        String email = membro.getEmail();
        boolean temEmail = email != null && !email.isBlank();

        boolean cpfDuplicado = novo
                ? membroRepository.existsByCpf(cpf)
                : membroRepository.existsByCpfAndIdNot(cpf, membro.getId());

        if (cpfDuplicado) {
            throw new RegraNegocioException("CPF já cadastrado.");
        }

        boolean emailDuplicado = temEmail && (novo
                ? membroRepository.existsByEmail(email)
                : membroRepository.existsByEmailAndIdNot(email, membro.getId()));

        if (emailDuplicado) {
            throw new RegraNegocioException("E-mail já cadastrado.");
        }

        // 3. Grava
        return membroRepository.save(membro);
    }

    // ---------- Situação ----------

    public void ativar(Long id) {
        Membro membro = buscarPorId(id);
        membro.setAtivo(true);
        membroRepository.save(membro);
    }

    public void desativar(Long id) {
        Membro membro = buscarPorId(id);

        if (ehUnicoAdministradorAtivo(membro)) {
            throw new RegraNegocioException("Não é possível desativar o único administrador ativo do sistema.");
        }

        membro.setAtivo(false);
        membroRepository.save(membro);
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

        // Exclui das "pontas" para o centro: o banco recusaria apagar o membro antes
        conta.ifPresent(usuario -> {
            refreshTokenRepository.deleteByUsuarioId(usuario.getId());
            usuarioRepository.delete(usuario);
        });

        presencaEventoRepository.deleteByMembroId(id);
        leituraAvisoRepository.deleteByMembroId(id);
        escalaItemRepository.deleteByMembroId(id);

        membroRepository.delete(membro);
    }

    // ---------- Apoio ----------

    private boolean ehUnicoAdministradorAtivo(Membro membro) {
        boolean ehAdministrador = usuarioRepository.findByMembroId(membro.getId())
                .filter(usuario -> usuario.isAdmin() && usuario.isAtivo())
                .isPresent();

        return ehAdministrador
                && usuarioRepository.countByAdminTrueAndAtivoTrueAndMembro_AtivoTrue() <= 1;
    }
}