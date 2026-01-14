package com.isinolsun.dto;

import com.isinolsun.tipler.BasvuruDurumu;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MobilBasvuruDTO {
    // --- MEVCUT ALANLAR ---
    private Long id;
    private Long ilanId;
    private String isBasligi;
    private String sirketAdi;
    private String sehir;
    private BasvuruDurumu durum;
    private String basvuruTarihi;
    private String mulakatTarihi;
    private String isverenNotu;

    // --- YENİ EKLENEN ADAY BİLGİLERİ (Mobilde görünmesi için) ---
    private String ad;
    private String soyad;
    private String meslek;
    private String ozetBilgi;
    private String telefon;
    private String email;
    private boolean cvVarMi;

    // Formatlayıcı
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // --- GÜNCELLENMİŞ CONSTRUCTOR ---
    public MobilBasvuruDTO(Long id, Long ilanId, String isBasligi, String sirketAdi, String sehir, 
                           BasvuruDurumu durum, LocalDateTime basvuruTarihiObj, 
                           LocalDateTime mulakatTarihiObj, String isverenNotu,
                           // Yeni parametreler buraya eklendi
                           String ad, String soyad, String meslek, String ozetBilgi,
                           String telefon, String email, boolean cvVarMi) {
        this.id = id;
        this.ilanId = ilanId;
        this.isBasligi = isBasligi;
        this.sirketAdi = sirketAdi;
        this.sehir = sehir;
        this.durum = durum;
        
        // Tarih formatlama
        this.basvuruTarihi = (basvuruTarihiObj != null) ? basvuruTarihiObj.format(formatter) : null;
        this.mulakatTarihi = (mulakatTarihiObj != null) ? mulakatTarihiObj.format(formatter) : null;
        
        this.isverenNotu = isverenNotu;

        // Yeni atamalar
        this.ad = ad;
        this.soyad = soyad;
        this.meslek = meslek;
        this.ozetBilgi = ozetBilgi;
        this.telefon = telefon;
        this.email = email;
        this.cvVarMi = cvVarMi;
    }

    // --- GETTER METODLARI ---
    public Long getId() { return id; }
    public Long getIlanId() { return ilanId; }
    public String getIsBasligi() { return isBasligi; }
    public String getSirketAdi() { return sirketAdi; }
    public String getSehir() { return sehir; }
    public BasvuruDurumu getDurum() { return durum; }
    public String getBasvuruTarihi() { return basvuruTarihi; }
    public String getMulakatTarihi() { return mulakatTarihi; } 
    public String getIsverenNotu() { return isverenNotu; } 

    // Yeni Getterlar
    public String getAd() { return ad; }
    public String getSoyad() { return soyad; }
    public String getMeslek() { return meslek; }
    public String getOzetBilgi() { return ozetBilgi; }
    public String getTelefon() { return telefon; }
    public String getEmail() { return email; }
    public boolean isCvVarMi() { return cvVarMi; }
}