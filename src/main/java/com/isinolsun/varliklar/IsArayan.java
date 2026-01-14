package com.isinolsun.varliklar;

import jakarta.persistence.*;

@Entity
@Table(name = "is_arayanlar")
public class IsArayan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "kullanici_id", referencedColumnName = "id")
    private Kullanici kullanici;

    @Column(nullable = false)
    private String ad;

    private String soyad;

    private String meslek;

    @Column(columnDefinition = "TEXT")
    private String ozet_bilgi;

    // --- CV ALANLARI ---
    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] cvDosya;

    private String cvDosyaAdi;

    // --- YENİ EKLENEN PROFIL RESMİ ALANI ---
    @Lob
    @Column(columnDefinition = "MEDIUMBLOB") // Resim dosyası için
    private byte[] profilResmi;

    // --- GETTER VE SETTERLAR ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Kullanici getKullanici() { return kullanici; }
    public void setKullanici(Kullanici kullanici) { this.kullanici = kullanici; }

    public String getAd() { return ad; }
    public void setAd(String ad) { this.ad = ad; }

    public String getSoyad() { return soyad; }
    public void setSoyad(String soyad) { this.soyad = soyad; }

    public String getMeslek() { return meslek; }
    public void setMeslek(String meslek) { this.meslek = meslek; }

    public String getOzet_bilgi() { return ozet_bilgi; }
    public void setOzet_bilgi(String ozet_bilgi) { this.ozet_bilgi = ozet_bilgi; }

    public byte[] getCvDosya() { return cvDosya; }
    public void setCvDosya(byte[] cvDosya) { this.cvDosya = cvDosya; }

    public String getCvDosyaAdi() { return cvDosyaAdi; }
    public void setCvDosyaAdi(String cvDosyaAdi) { this.cvDosyaAdi = cvDosyaAdi; }

    // --- YENİ PROFIL RESMİ GETTER/SETTER ---
    public byte[] getProfilResmi() { return profilResmi; }
    public void setProfilResmi(byte[] profilResmi) { this.profilResmi = profilResmi; }
}