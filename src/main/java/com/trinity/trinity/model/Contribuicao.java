package com.trinity.trinity.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "contribuicoes")
public class Contribuicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 25)
    private String identificador;

    @ManyToOne(optional = false)
    @JoinColumn(name = "membro_id")
    private Membro membro;

    @ManyToOne(optional = false)
    @JoinColumn(name = "categoria_id")
    private CategoriaFinanceira categoria;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private StatusContribuicao status;

    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm;

    @OneToOne
    @JoinColumn(name = "lancamento_id")
    private Lancamento lancamento;

    protected Contribuicao() {
    }

    public Contribuicao(Membro membro, CategoriaFinanceira categoria, BigDecimal valor) {
        this.identificador = gerarIdentificador();
        this.membro = membro;
        this.categoria = categoria;
        this.valor = valor;
        this.status = StatusContribuicao.AGUARDANDO_PAGAMENTO;
        this.criadaEm = LocalDateTime.now();
    }

    /**
     * Código que vai no PIX e aparece no extrato do banco.
     * O padrão PIX aceita só letras e números, com até 25 caracteres.
     */
    private static String gerarIdentificador() {
        String aleatorio = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return "TRINITY" + aleatorio.toUpperCase();
    }

    public void informarPagamento() {
        this.status = StatusContribuicao.AGUARDANDO_CONFIRMACAO;
    }

    public void confirmar(Lancamento lancamento) {
        this.lancamento = lancamento;
        this.status = StatusContribuicao.CONFIRMADA;
    }

    public void rejeitar() {
        this.status = StatusContribuicao.REJEITADA;
    }

    public Long getId() {
        return id;
    }

    public String getIdentificador() {
        return identificador;
    }

    public Membro getMembro() {
        return membro;
    }

    public CategoriaFinanceira getCategoria() {
        return categoria;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public StatusContribuicao getStatus() {
        return status;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public Lancamento getLancamento() {
        return lancamento;
    }
}