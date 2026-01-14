package com.isinolsun.varliklar;

import jakarta.persistence.*;

@Entity
@Table(name = "paketler")
public class Paket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ad;       // Örn: "Hızlı Başlangıç Paketi"
    private int gun;         // Örn: 3 (3 gün vitrinde kalacak)
    private double fiyat;    // Örn: 50.0

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAd() { return ad; }
    public void setAd(String ad) { this.ad = ad; }
    public int getGun() { return gun; }
    public void setGun(int gun) { this.gun = gun; }
    public double getFiyat() { return fiyat; }
    public void setFiyat(double fiyat) { this.fiyat = fiyat; }
}