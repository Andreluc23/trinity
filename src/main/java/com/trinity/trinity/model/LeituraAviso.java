package com.trinity.trinity.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "leituras_aviso",
        uniqueConstraints = @UniqueConstraint(columnNames = {"aviso_id", "membro_id"}))
public class LeituraAviso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aviso_id")
    private Aviso aviso;

    @ManyToOne(optional = false)
    @JoinColumn(name = "membro_id")
    private Membro membro;

    @Column(name = "lido_em", nullable = false)
    private LocalDateTime lidoEm;

    protected LeituraAviso() {
    }

    public LeituraAviso(Aviso aviso, Membro membro) {
        this.aviso = aviso;
        this.membro = membro;
        this.lidoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Aviso getAviso() {
        return aviso;
    }

    public Membro getMembro() {
        return membro;
    }

    public LocalDateTime getLidoEm() {
        return lidoEm;
    }
}