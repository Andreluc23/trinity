package com.trinity.trinity.model;

import jakarta.persistence.*;

@Entity
@Table(name = "escala_itens")
public class EscalaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "escala_id", nullable = false)
    private Escala escala;

    @ManyToOne(optional = false)
    @JoinColumn(name = "membro_id", nullable = false)
    private Membro membro;

    @ManyToOne(optional = false)
    @JoinColumn(name = "funcao_musical_id", nullable = false)
    private FuncaoMusical funcaoMusical;

    public EscalaItem() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Escala getEscala() {
        return escala;
    }

    public void setEscala(Escala escala) {
        this.escala = escala;
    }

    public Membro getMembro() {
        return membro;
    }

    public void setMembro(Membro membro) {
        this.membro = membro;
    }

    public FuncaoMusical getFuncaoMusical() {
        return funcaoMusical;
    }

    public void setFuncaoMusical(FuncaoMusical funcaoMusical) {
        this.funcaoMusical = funcaoMusical;
    }
}