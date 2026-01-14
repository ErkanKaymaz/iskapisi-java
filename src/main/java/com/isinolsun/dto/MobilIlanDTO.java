package com.isinolsun.dto;

import java.time.LocalDateTime;
import java.util.Base64;

public class MobilIlanDTO {

    private Long id;
    private String baslik;
    private String sirketAdi;
    private String sehir;
    private String maas;
    private String yayinlanmaTarihi;
    private String logoBase64;
    
    // --- YENİ EKLENENLER ---
    private String aciklama; 
    private Double enlem;    
    private Double boylam;   
    
    // ⭐ SPONSORLU İLAN İÇİN GEREKLİ ⭐
    private LocalDateTime vitrinBitisTarihi;

    public MobilIlanDTO(Long id, String baslik, String sirketAdi, String sehir, String maas, 
                        LocalDateTime yayinlanmaTarihi, byte[] logoBytes, 
                        String aciklama, Double enlem, Double boylam,
                        LocalDateTime vitrinBitisTarihi) { // <-- Parametre Eklendi
        this.id = id;
        this.baslik = baslik;
        this.sirketAdi = sirketAdi;
        this.sehir = sehir;
        this.maas = maas;
        this.yayinlanmaTarihi = yayinlanmaTarihi != null ? yayinlanmaTarihi.toString() : "";
        this.aciklama = aciklama; 
        this.enlem = enlem;       
        this.boylam = boylam;     
        this.vitrinBitisTarihi = vitrinBitisTarihi; // <-- Eşleştirildi

        if (logoBytes != null && logoBytes.length > 0) {
            this.logoBase64 = Base64.getEncoder().encodeToString(logoBytes);
        } else {
            this.logoBase64 = null;
        }
    }

    // Getter Metotları
    public Long getId() { return id; }
    public String getBaslik() { return baslik; }
    public String getSirketAdi() { return sirketAdi; }
    public String getSehir() { return sehir; }
    public String getMaas() { return maas; }
    public String getYayinlanmaTarihi() { return yayinlanmaTarihi; }
    public String getLogoBase64() { return logoBase64; }
    public String getAciklama() { return aciklama; }
    public Double getEnlem() { return enlem; }
    public Double getBoylam() { return boylam; }
    
    // Yeni Getter
    public LocalDateTime getVitrinBitisTarihi() { return vitrinBitisTarihi; }
}