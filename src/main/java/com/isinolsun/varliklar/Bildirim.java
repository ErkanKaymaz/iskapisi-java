package com.isinolsun.varliklar;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bildirimler")
public class Bildirim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mesaj;

    private LocalDateTime tarih;

    // Bildirimin kime gideceği
    @ManyToOne
    @JoinColumn(name = "kullanici_id")
    private Kullanici kullanici;

    public Bildirim() {
        this.tarih = LocalDateTime.now();
    }

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMesaj() { return mesaj; }
    public void setMesaj(String mesaj) { this.mesaj = mesaj; }
    public LocalDateTime getTarih() { return tarih; }
    public void setTarih(LocalDateTime tarih) { this.tarih = tarih; }
    public Kullanici getKullanici() { return kullanici; }
    public void setKullanici(Kullanici kullanici) { this.kullanici = kullanici; }
}