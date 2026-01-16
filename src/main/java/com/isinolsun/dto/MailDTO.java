package com.isinolsun.dto;

public class MailDTO {
    private String email;
    private String baslik;
    private String mesaj;


    public MailDTO() {}


    public MailDTO(String email, String baslik, String mesaj) {
        this.email = email;
        this.baslik = baslik;
        this.mesaj = mesaj;
    }

    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBaslik() { return baslik; }
    public void setBaslik(String baslik) { this.baslik = baslik; }

    public String getMesaj() { return mesaj; }
    public void setMesaj(String mesaj) { this.mesaj = mesaj; }
}