package com.isinolsun.varliklar;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "favoriler")
public class Favori {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "is_arayan_id")
    private IsArayan isArayan;

    @ManyToOne
    @JoinColumn(name = "is_ilani_id")
    private IsIlani isIlani;

    private LocalDateTime eklenmeTarihi;

    public Favori() {
        this.eklenmeTarihi = LocalDateTime.now();
    }

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public IsArayan getIsArayan() { return isArayan; }
    public void setIsArayan(IsArayan isArayan) { this.isArayan = isArayan; }
    public IsIlani getIsIlani() { return isIlani; }
    public void setIsIlani(IsIlani isIlani) { this.isIlani = isIlani; }
    public LocalDateTime getEklenmeTarihi() { return eklenmeTarihi; }
}