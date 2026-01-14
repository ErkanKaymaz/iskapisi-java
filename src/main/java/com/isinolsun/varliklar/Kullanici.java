package com.isinolsun.varliklar;

import com.isinolsun.tipler.KullaniciRolu;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "kullanicilar")
public class Kullanici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String sifre;

    private String telefon;

    @Enumerated(EnumType.STRING)
    private KullaniciRolu rol;

    @Column(name = "kayit_tarihi")
    private LocalDateTime kayitTarihi;

    @PrePersist
    protected void onCreate() {
        kayitTarihi = LocalDateTime.now();
    }

    // --- MANUEL GETTER VE SETTERLAR ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSifre() { return sifre; } // İşte aradığı metod bu!
    public void setSifre(String sifre) { this.sifre = sifre; }

    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }

    public KullaniciRolu getRol() { return rol; }
    public void setRol(KullaniciRolu rol) { this.rol = rol; }

    public LocalDateTime getKayitTarihi() { return kayitTarihi; }
    public void setKayitTarihi(LocalDateTime kayitTarihi) { this.kayitTarihi = kayitTarihi; }
}