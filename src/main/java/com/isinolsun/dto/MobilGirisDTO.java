package com.isinolsun.dto;

import com.isinolsun.tipler.KullaniciRolu;

public class MobilGirisDTO {
    private Long id;
    private String email;
    private String ad;
    private String telefon;
    private KullaniciRolu rol;
    private String logoBase64;

    public MobilGirisDTO(Long id, String email, String ad, String telefon, KullaniciRolu rol, String logoBase64) {
        this.id = id;
        this.email = email;
        this.ad = ad;
        this.telefon = telefon;
        this.rol = rol;
        this.logoBase64 = logoBase64;
    }

    // Getterlar
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getAd() { return ad; }
    public String getTelefon() { return telefon; }
    public KullaniciRolu getRol() { return rol; }
    public String getLogoBase64() { return logoBase64; }
}