package com.trinity.trinity.config;

import com.trinity.trinity.model.avisos.Aviso;
import com.trinity.trinity.model.avisos.LeituraAviso;
import com.trinity.trinity.model.escalas.Escala;
import com.trinity.trinity.model.escalas.EscalaItem;
import com.trinity.trinity.model.escalas.FuncaoMusical;
import com.trinity.trinity.model.eventos.Evento;
import com.trinity.trinity.model.eventos.PresencaEvento;
import com.trinity.trinity.model.eventos.RespostaPresenca;
import com.trinity.trinity.model.financeiro.CategoriaFinanceira;
import com.trinity.trinity.model.financeiro.Contribuicao;
import com.trinity.trinity.model.financeiro.Lancamento;
import com.trinity.trinity.model.financeiro.OrigemLancamento;
import com.trinity.trinity.model.financeiro.TipoLancamento;
import com.trinity.trinity.model.membros.Endereco;
import com.trinity.trinity.model.membros.Membro;
import com.trinity.trinity.model.patrimonio.Patrimonio;
import com.trinity.trinity.model.usuarios.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Carrega dados fictícios para demonstração do sistema.
 *
 * Só roda quando trinity.demo=true (variável TRINITY_DEMO=true) e somente se o banco
 * ainda não tiver nenhum membro real. Nunca apaga nem altera dados existentes.
 * As datas são relativas a hoje, então o dashboard e as listas ficam sempre atuais.
 */
@Component
public class DadosDemonstracao implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosDemonstracao.class);

    private static final String SENHA_DEMO = "Trinity@123";
    private static final String CPF_ADMINISTRADOR_INICIAL = "00000000000";

    // nome, e-mail, perfil de acesso
    private static final String[][] CONTAS_FIXAS = {
            {"Pr. Carlos Almeida", "admin@trinity.demo", "ADMIN"},
            {"Marta Tesoureira", "tesouraria@trinity.demo", "FINANCEIRO"},
            {"Sônia Secretária", "secretaria@trinity.demo", "SECRETARIA"},
            {"Davi Ministério de Louvor", "louvor@trinity.demo", "LOUVOR"},
            {"Membro Demonstração", "membro@trinity.demo", "MEMBRO"}
    };

    private static final String[] NOMES = {
            "Ana Beatriz Souza", "Bruno Henrique Lima", "Camila Fernandes", "Daniel Oliveira Santos",
            "Eduarda Martins", "Felipe Augusto Rocha", "Gabriela Ribeiro", "Heitor Almeida",
            "Isabela Carvalho", "João Pedro Barbosa", "Karen Cristina Dias", "Lucas Gabriel Ferreira",
            "Mariana Costa", "Natália Pereira", "Otávio Mendes", "Paula Regina Nunes",
            "Rafael Moreira", "Sabrina Teixeira", "Tiago Cardoso", "Úrsula Monteiro", "Vitor Hugo Araújo"
    };

    private static final int[] INATIVOS = {10, 13, 19};
    private static final int MEMBROS_COM_CONTA = 8;

    private static final String[] RUAS = {"Rua das Palmeiras", "Avenida Paraná", "Rua Ipê",
            "Rua Santos Dumont", "Avenida Brasil", "Rua Pedro Álvares Cabral"};
    private static final String[] BAIRROS = {"Centro", "Zona I", "Zona II", "Jardim Alvorada", "Parque Bandeirantes"};

    // título, mensagem
    private static final String[][] AVISOS = {
            {"Culto de Santa Ceia", "No próximo domingo teremos Santa Ceia. Chegue com 15 minutos de antecedência."},
            {"Escala de louvor atualizada", "A escala do mês já está disponível na tela de Escalas. Confira o seu dia."},
            {"Campanha do agasalho", "Estamos recebendo agasalhos e cobertores na secretaria até o fim do mês."},
            {"Reunião de líderes", "Reunião de líderes de ministério na sexta-feira, às 20h, na sala de reuniões."},
            {"Novo horário da escola bíblica", "A escola bíblica dominical passa a começar às 9h. Contamos com a sua presença."},
            {"Ensaio geral do ministério de louvor", "Ensaio geral no sábado, às 16h. Todos os escalados devem comparecer."},
            {"Aviso antigo (já encerrado)", "Este aviso foi encerrado e está desativado para os membros."},
            {"Batismo", "Quem deseja ser batizado deve procurar a secretaria e preencher a ficha de inscrição."},
            {"Cesta básica", "Arrecadação de alimentos não perecíveis para as famílias atendidas pela igreja."},
            {"Manutenção do templo", "No sábado haverá mutirão de limpeza e pequenos reparos. Traga a sua disposição."},
            {"Congresso de jovens", "Inscrições abertas para o congresso de jovens. Vagas limitadas."},
            {"Retiro cancelado", "O retiro desta data foi cancelado e será remarcado. Aguarde novas informações."},
            {"Oração pelas famílias", "Toda quarta-feira, às 19h30, teremos um momento de oração pelas famílias."},
            {"Prestação de contas", "A prestação de contas do trimestre está disponível com a tesouraria."},
            {"Bem-vindos!", "Seja bem-vindo ao Trinity. Aqui você acompanha avisos, eventos e escalas da igreja."}
    };

    // título, descrição, local, dias em relação a hoje, horário
    private static final String[][] EVENTOS = {
            {"Culto de Domingo", "Culto de celebração.", "Templo", "-31", "19:00"},
            {"Noite de Louvor", "Uma noite dedicada ao louvor.", "Templo", "-24", "19:30"},
            {"Encontro de Casais", "Encontro com café da manhã.", "Salão social", "-17", "08:30"},
            {"Culto de Domingo", "Culto de celebração.", "Templo", "-10", "19:00"},
            {"Mutirão de Limpeza", "Limpeza geral do templo e do salão.", "Templo", "-3", "08:00"},
            {"Culto de Domingo", "Culto de celebração com Santa Ceia.", "Templo", "0", "19:30"},
            {"Oração das Famílias", "Momento de oração e comunhão.", "Sala de oração", "2", "19:30"},
            {"Escola Bíblica", "Estudo da Palavra para todas as idades.", "Salas de aula", "4", "09:00"},
            {"Culto de Domingo", "Culto de celebração.", "Templo", "7", "19:00"},
            {"Ensaio do Ministério de Louvor", "Ensaio geral.", "Templo", "9", "16:00"},
            {"Culto de Jovens", "Culto com programação para os jovens.", "Salão social", "11", "20:00"},
            {"Retiro de Jovens", "Retiro cancelado e a remarcar.", "Chácara Esperança", "14", "08:00"},
            {"Culto de Domingo", "Culto de celebração.", "Templo", "16", "19:00"},
            {"Café com a Liderança", "Reunião de líderes com café.", "Sala de reuniões", "21", "08:00"},
            {"Culto de Ação de Graças", "Culto especial de gratidão.", "Templo", "28", "19:30"}
    };

    // título, observação, dias em relação a hoje, horário
    private static final String[][] ESCALAS = {
            {"Culto de Domingo - Noite", "Escala já realizada.", "-14", "19:00"},
            {"Culto de Domingo - Manhã", "Escala já realizada.", "-7", "09:00"},
            {"Culto de Domingo - Noite", "Chegar 1 hora antes para a passagem de som.", "2", "19:00"},
            {"Culto de Jovens", "Repertório definido pela liderança.", "6", "20:00"},
            {"Culto de Domingo - Manhã", "Traga o seu instrumento afinado.", "9", "09:00"},
            {"Culto de Ação de Graças", "Ensaio no sábado anterior, às 16h.", "16", "19:30"}
    };

    private static final String[] FUNCOES = {"Vocal", "Violão", "Guitarra", "Baixo", "Bateria", "Teclado", "Violino", "Saxofone"};

    // nome, categoria, quantidade, estado, localização
    private static final String[][] PATRIMONIO = {
            {"Caixa de som ativa 15\"", "Som", "4", "Bom", "Templo"},
            {"Mesa de som digital 16 canais", "Som", "1", "Bom", "Templo"},
            {"Microfone sem fio", "Som", "4", "Bom", "Templo"},
            {"Microfone com fio", "Som", "6", "Regular", "Templo"},
            {"Pedestal de microfone", "Som", "8", "Bom", "Templo"},
            {"Violão", "Instrumentos", "3", "Bom", "Templo"},
            {"Guitarra", "Instrumentos", "2", "Bom", "Templo"},
            {"Baixo", "Instrumentos", "1", "Regular", "Templo"},
            {"Bateria completa", "Instrumentos", "1", "Bom", "Templo"},
            {"Teclado 61 teclas", "Instrumentos", "2", "Bom", "Templo"},
            {"Bancos do templo", "Mobiliário", "120", "Bom", "Templo"},
            {"Mesas dobráveis", "Mobiliário", "15", "Regular", "Salão social"},
            {"Cadeiras plásticas", "Mobiliário", "80", "Regular", "Salão social"},
            {"Púlpito", "Mobiliário", "1", "Bom", "Templo"},
            {"Computador da secretaria", "Informática", "2", "Bom", "Secretaria"},
            {"Projetor multimídia", "Informática", "1", "Bom", "Templo"},
            {"Impressora multifuncional", "Informática", "1", "Regular", "Secretaria"},
            {"Ar-condicionado split", "Equipamentos", "3", "Bom", "Templo"},
            {"Ventilador de coluna", "Equipamentos", "6", "Ruim", "Salão social"},
            {"Armário de arquivos", "Mobiliário", "2", "Ruim", "Secretaria"}
    };

    private final PasswordEncoder passwordEncoder;
    private final boolean ligado;
    private final String emailAdministradorReal;

    @PersistenceContext
    private EntityManager em;

    private final Random aleatorio = new Random(2026);
    private final LocalDate hoje = LocalDate.now();
    private final Set<String> cpfsUsados = new HashSet<>();

    private final List<Membro> membros = new ArrayList<>();
    private final List<Membro> membrosAtivos = new ArrayList<>();
    private final List<Membro> membrosComConta = new ArrayList<>();
    private final Map<String, Membro> contasFixas = new HashMap<>();
    private final Map<String, CategoriaFinanceira> categorias = new HashMap<>();

    public DadosDemonstracao(PasswordEncoder passwordEncoder,
                             @Value("${trinity.demo:false}") boolean ligado,
                             @Value("${trinity.admin.email:}") String emailAdministradorReal) {
        this.passwordEncoder = passwordEncoder;
        this.ligado = ligado;
        this.emailAdministradorReal = emailAdministradorReal == null ? "" : emailAdministradorReal.trim();
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!ligado) {
            return;
        }

        Long membrosReais = em.createQuery(
                        "select count(m) from Membro m where m.cpf <> :cpf", Long.class)
                .setParameter("cpf", CPF_ADMINISTRADOR_INICIAL)
                .getSingleResult();

        if (membrosReais > 0) {
            log.warn("Dados de demonstração NÃO carregados: o banco já possui membros. "
                    + "Use um banco vazio (ex.: outro arquivo em ./data) para a demonstração.");
            return;
        }

        criarMembrosEContas();
        criarCategoriasELancamentos();
        criarContribuicoes();
        criarAvisos();
        criarEventos();
        criarEscalas();
        criarPatrimonio();

        log.warn("Dados de demonstração carregados: {} membros. Senha de todas as contas: {}",
                membros.size(), SENHA_DEMO);
        log.warn("Logins: tesouraria@trinity.demo (financeiro), secretaria@trinity.demo (membros/avisos/eventos), "
                        + "louvor@trinity.demo (escalas), membro@trinity.demo (membro comum){}",
                emailAdministradorReal.isBlank()
                        ? ", admin@trinity.demo (administrador)"
                        : ". Administrador: " + emailAdministradorReal);
    }

    // ---------- Membros e contas de acesso ----------

    private void criarMembrosEContas() {
        String hash = passwordEncoder.encode(SENHA_DEMO);

        for (int i = 0; i < CONTAS_FIXAS.length; i++) {
            String[] dados = CONTAS_FIXAS[i];
            LocalDate nascimento = LocalDate.of(1972 + i * 4, (i * 3) % 12 + 1, 5 + i * 5);
            Membro membro = novoMembro(dados[0], dados[1], nascimento, true, i);
            contasFixas.put(dados[2], membro);

            boolean administradorFicaPorContaDoSistema = dados[2].equals("ADMIN") && !emailAdministradorReal.isBlank();
            if (!administradorFicaPorContaDoSistema) {
                criarConta(membro, dados[2], hash);
            }
        }

        for (int i = 0; i < NOMES.length; i++) {
            boolean ativo = !ehInativo(i);

            // Alguns fazem aniversário no mês atual, para o card de aniversariantes
            int mes = i % 7 == 0 ? hoje.getMonthValue() : (i * 5) % 12 + 1;
            LocalDate nascimento = LocalDate.of(1955 + (i * 3) % 50, mes, 1 + (i * 7) % 28);

            Membro membro = novoMembro(NOMES[i], emailDoNome(NOMES[i]), nascimento, ativo, i + CONTAS_FIXAS.length);

            if (ativo && i < MEMBROS_COM_CONTA) {
                criarConta(membro, "MEMBRO", hash);
            }
        }
    }

    private static boolean ehInativo(int indice) {
        for (int inativo : INATIVOS) {
            if (inativo == indice) {
                return true;
            }
        }
        return false;
    }

    private Membro novoMembro(String nome, String email, LocalDate nascimento, boolean ativo, int indice) {
        Endereco endereco = new Endereco();
        endereco.setCep("87501" + String.format("%03d", aleatorio.nextInt(1000)));
        endereco.setLogradouro(RUAS[aleatorio.nextInt(RUAS.length)]);
        endereco.setNumero(String.valueOf(1 + aleatorio.nextInt(999)));
        endereco.setBairro(BAIRROS[aleatorio.nextInt(BAIRROS.length)]);
        endereco.setCidade("Umuarama");
        endereco.setUf("PR");

        Membro membro = new Membro();
        membro.setNomeCompleto(nome);
        membro.setEmail(email);
        membro.setDataNascimento(nascimento);
        membro.setCpf(gerarCpf());
        membro.setTelefone(gerarTelefone(indice));
        membro.setEndereco(endereco);
        membro.setAtivo(ativo);
        em.persist(membro);

        membros.add(membro);
        if (ativo) {
            membrosAtivos.add(membro);
        }
        return membro;
    }

    private void criarConta(Membro membro, String perfil, String hashSenha) {
        Usuario usuario = new Usuario();
        usuario.setMembro(membro);
        usuario.setSenha(hashSenha);
        usuario.setAtivo(true);

        switch (perfil) {
            case "ADMIN" -> {
                usuario.setAdmin(true);
                usuario.concederTodasAsPermissoes();
            }
            case "FINANCEIRO" -> usuario.setGerenciarFinanceiro(true);
            case "SECRETARIA" -> {
                usuario.setGerenciarMembros(true);
                usuario.setGerenciarAvisos(true);
                usuario.setGerenciarEventos(true);
            }
            case "LOUVOR" -> {
                usuario.setAcessoEscalas(true);
                usuario.setGerenciarEscalas(true);
            }
            default -> {
                // membro comum: sem permissões de gerenciamento
            }
        }

        em.persist(usuario);
        membrosComConta.add(membro);
    }

    // ---------- Financeiro ----------

    private void criarCategoriasELancamentos() {
        CategoriaFinanceira dizimo = categoria("Dízimo", TipoLancamento.ENTRADA);
        CategoriaFinanceira oferta = categoria("Oferta", TipoLancamento.ENTRADA);
        CategoriaFinanceira doacao = categoria("Doação", TipoLancamento.ENTRADA);
        CategoriaFinanceira energia = categoria("Energia elétrica", TipoLancamento.SAIDA);
        CategoriaFinanceira agua = categoria("Água", TipoLancamento.SAIDA);
        CategoriaFinanceira manutencao = categoria("Manutenção", TipoLancamento.SAIDA);
        CategoriaFinanceira limpeza = categoria("Material de limpeza", TipoLancamento.SAIDA);
        CategoriaFinanceira acaoSocial = categoria("Ação social", TipoLancamento.SAIDA);

        String[] ofertas = {"Oferta do culto de domingo", "Oferta de missões", "Oferta de gratidão", "Oferta do culto de jovens"};

        for (int atras = 5; atras >= 0; atras--) {
            YearMonth mes = YearMonth.from(hoje).minusMonths(atras);

            for (int k = 0; k < 4; k++) {
                Membro dizimista = membrosAtivos.get(aleatorio.nextInt(membrosAtivos.size()));
                boolean ativo = !(k == 1 && (atras == 3 || atras == 1));
                lancamento(mes, 3 + k * 7, "Dízimo - " + dizimista.getNomeCompleto(),
                        valor(150, 800), dizimo, dizimista, ativo);
                lancamento(mes, 5 + k * 7, ofertas[k], valor(80, 400), oferta, null, true);
            }

            lancamento(mes, 15, "Doação para a reforma", valor(200, 1500), doacao, null, true);
            lancamento(mes, 10, "Conta de energia", valor(380, 520), energia, null, true);
            lancamento(mes, 12, "Conta de água", valor(90, 160), agua, null, true);
            lancamento(mes, 18, "Manutenção do templo", valor(150, 600), manutencao, null, true);
            lancamento(mes, 20, "Material de limpeza", valor(60, 200), limpeza, null, true);
            lancamento(mes, 25, "Cestas básicas para famílias", valor(200, 500), acaoSocial, null, true);
        }
    }

    private CategoriaFinanceira categoria(String nome, TipoLancamento tipo) {
        CategoriaFinanceira categoria = new CategoriaFinanceira();
        categoria.setNome(nome);
        categoria.setTipo(tipo);
        categoria.setAtivo(true);
        em.persist(categoria);
        categorias.put(nome, categoria);
        return categoria;
    }

    private Lancamento lancamento(YearMonth mes, int dia, String descricao, BigDecimal valor,
                                  CategoriaFinanceira categoria, Membro membro, boolean ativo) {
        LocalDate data = mes.atDay(Math.min(dia, mes.lengthOfMonth()));
        if (data.isAfter(hoje)) {
            data = hoje;
        }

        Lancamento lancamento = new Lancamento();
        lancamento.setDescricao(descricao);
        lancamento.setValor(valor);
        lancamento.setData(data);
        lancamento.setCategoria(categoria);
        lancamento.setTipo(categoria.getTipo());
        lancamento.setMembro(membro);
        lancamento.setOrigem(OrigemLancamento.MANUAL);
        lancamento.setAtivo(ativo);
        em.persist(lancamento);
        return lancamento;
    }

    /** Valor em reais, de 10 em 10, entre o mínimo e o máximo. */
    private BigDecimal valor(int minimo, int maximo) {
        int reais = minimo + aleatorio.nextInt((maximo - minimo) / 10 + 1) * 10;
        return BigDecimal.valueOf(reais * 100L, 2);
    }

    private void criarContribuicoes() {
        CategoriaFinanceira dizimo = categorias.get("Dízimo");
        CategoriaFinanceira oferta = categorias.get("Oferta");

        Membro membro = contasFixas.get("MEMBRO");
        Membro secretaria = contasFixas.get("SECRETARIA");
        Membro louvor = contasFixas.get("LOUVOR");
        Membro tesouraria = contasFixas.get("FINANCEIRO");

        // Aguardando confirmação (aparecem no selo do menu do financeiro)
        informada(new Contribuicao(membro, dizimo, BigDecimal.valueOf(10000, 2)));
        informada(new Contribuicao(louvor, oferta, BigDecimal.valueOf(5000, 2)));
        informada(new Contribuicao(secretaria, dizimo, BigDecimal.valueOf(20000, 2)));

        // Gerada, mas o membro ainda não informou o pagamento
        em.persist(new Contribuicao(membro, oferta, BigDecimal.valueOf(3000, 2)));

        // Rejeitada
        Contribuicao rejeitada = new Contribuicao(secretaria, oferta, BigDecimal.valueOf(1000, 2));
        rejeitada.informarPagamento();
        rejeitada.rejeitar();
        em.persist(rejeitada);

        // Confirmada: gera o lançamento de PIX, como no fluxo real
        Contribuicao confirmada = new Contribuicao(tesouraria, dizimo, BigDecimal.valueOf(30000, 2));
        confirmada.informarPagamento();
        em.persist(confirmada);

        Lancamento entradaPix = new Lancamento();
        entradaPix.setDescricao("Contribuição PIX " + confirmada.getIdentificador());
        entradaPix.setValor(confirmada.getValor());
        entradaPix.setData(hoje.minusDays(2));
        entradaPix.setCategoria(dizimo);
        entradaPix.setTipo(TipoLancamento.ENTRADA);
        entradaPix.setMembro(tesouraria);
        entradaPix.setOrigem(OrigemLancamento.PIX);
        em.persist(entradaPix);

        confirmada.confirmar(entradaPix);
    }

    private void informada(Contribuicao contribuicao) {
        contribuicao.informarPagamento();
        em.persist(contribuicao);
    }

    // ---------- Avisos ----------

    private void criarAvisos() {
        for (int i = 0; i < AVISOS.length; i++) {
            Aviso aviso = new Aviso();
            aviso.setTitulo(AVISOS[i][0]);
            aviso.setMensagem(AVISOS[i][1]);
            aviso.setDataPublicacao(hoje.minusDays(i * 4L));
            aviso.setAtivo(i != 6 && i != 11);
            em.persist(aviso);

            if (aviso.isAtivo()) {
                for (Membro leitor : membrosComConta) {
                    if (aleatorio.nextInt(100) < 55) {
                        em.persist(new LeituraAviso(aviso, leitor));
                    }
                }
            }
        }
    }

    // ---------- Eventos ----------

    private void criarEventos() {
        for (String[] dados : EVENTOS) {
            int dias = Integer.parseInt(dados[3]);

            Evento evento = new Evento();
            evento.setTitulo(dados[0]);
            evento.setDescricao(dados[1]);
            evento.setLocal(dados[2]);
            evento.setData(hoje.plusDays(dias));
            evento.setHorario(LocalTime.parse(dados[4]));
            evento.setAtivo(!dados[0].equals("Retiro de Jovens"));
            em.persist(evento);

            if (evento.isAtivo() && dias >= 0) {
                for (Membro membro : membrosComConta) {
                    if (aleatorio.nextInt(100) < 60) {
                        RespostaPresenca resposta = aleatorio.nextInt(100) < 75
                                ? RespostaPresenca.VOU
                                : RespostaPresenca.NAO_VOU;
                        em.persist(new PresencaEvento(evento, membro, resposta));
                    }
                }
            }
        }
    }

    // ---------- Escalas ----------

    private void criarEscalas() {
        List<FuncaoMusical> funcoes = new ArrayList<>();
        for (String nome : FUNCOES) {
            FuncaoMusical funcao = new FuncaoMusical();
            funcao.setNome(nome);
            funcao.setAtivo(true);
            em.persist(funcao);
            funcoes.add(funcao);
        }

        for (String[] dados : ESCALAS) {
            Escala escala = new Escala();
            escala.setTitulo(dados[0]);
            escala.setObservacao(dados[1]);
            escala.setData(hoje.plusDays(Integer.parseInt(dados[2])));
            escala.setHorario(LocalTime.parse(dados[3]));
            escala.setAtivo(true);
            em.persist(escala);

            // Mesma regra do sistema: cada membro e cada função aparecem uma só vez na escala
            List<Membro> sorteados = new ArrayList<>(membrosAtivos);
            List<FuncaoMusical> funcoesSorteadas = new ArrayList<>(funcoes);
            Collections.shuffle(sorteados, aleatorio);
            Collections.shuffle(funcoesSorteadas, aleatorio);

            int integrantes = 4 + aleatorio.nextInt(3);
            for (int i = 0; i < integrantes; i++) {
                EscalaItem item = new EscalaItem();
                item.setEscala(escala);
                item.setMembro(sorteados.get(i));
                item.setFuncaoMusical(funcoesSorteadas.get(i));
                em.persist(item);
            }
        }
    }

    // ---------- Patrimônio ----------

    private void criarPatrimonio() {
        for (int i = 0; i < PATRIMONIO.length; i++) {
            String[] dados = PATRIMONIO[i];

            Patrimonio item = new Patrimonio();
            item.setNome(dados[0]);
            item.setCategoria(dados[1]);
            item.setQuantidade(Integer.parseInt(dados[2]));
            item.setEstadoConservacao(dados[3]);
            item.setLocalizacao(dados[4]);
            item.setAtivo(i != 18);
            em.persist(item);
        }
    }

    // ---------- Apoio ----------

    /** Gera um CPF válido (os dois últimos dígitos saem do mesmo cálculo da validação do sistema). */
    private String gerarCpf() {
        while (true) {
            int[] d = new int[11];
            for (int i = 0; i < 9; i++) {
                d[i] = aleatorio.nextInt(10);
            }
            d[9] = digitoVerificador(d, 9);
            d[10] = digitoVerificador(d, 10);

            String cpf = Arrays.stream(d).mapToObj(String::valueOf).collect(Collectors.joining());
            boolean todosIguais = cpf.chars().distinct().count() == 1;

            if (!todosIguais && cpfsUsados.add(cpf)) {
                return cpf;
            }
        }
    }

    private static int digitoVerificador(int[] d, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += d[i] * (quantidade + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }

    /** Guardado só com números, como no cadastro real: celular (11 dígitos) ou fixo (10). */
    private String gerarTelefone(int indice) {
        if (indice % 6 == 0) {
            return "4436" + String.format("%06d", aleatorio.nextInt(1_000_000));
        }
        return "449" + String.format("%08d", aleatorio.nextInt(100_000_000));
    }

    /** "João Pedro Barbosa" vira "joao.barbosa@exemplo.com.br". */
    private static String emailDoNome(String nome) {
        String semAcento = Normalizer.normalize(nome, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
        String[] partes = semAcento.split(" ");
        return partes[0] + "." + partes[partes.length - 1] + "@exemplo.com.br";
    }
}