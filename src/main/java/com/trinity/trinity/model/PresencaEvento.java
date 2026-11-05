package com.trinity.trinity.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "presencas_evento",
        uniqueConstraints = @UniqueConstraint(columnNames = {"evento_id", "membro_id"}))
public class PresencaEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "evento_id")
    private Evento evento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "membro_id")
    private Membro membro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RespostaPresenca resposta;

    @Column(name = "respondida_em", nullable = false)
    private LocalDateTime respondidaEm;

    protected PresencaEvento() {
    }

    public PresencaEvento(Evento evento, Membro membro, RespostaPresenca resposta) {
        this.evento = evento;
        this.membro = membro;
        responder(resposta);
    }

    public void responder(RespostaPresenca resposta) {
        this.resposta = resposta;
        this.respondidaEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Evento getEvento() {
        return evento;
    }

    public Membro getMembro() {
        return membro;
    }

    public RespostaPresenca getResposta() {
        return resposta;
    }

    public LocalDateTime getRespondidaEm() {
        return respondidaEm;
    }
}