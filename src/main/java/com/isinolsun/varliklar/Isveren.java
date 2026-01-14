package com.isinolsun.varliklar;

import jakarta.persistence.*;
import java.util.Base64;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "isverenler")
public class Isveren {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "kullanici_id", referencedColumnName = "id")
    private Kullanici kullanici;

    @Column(name = "sirket_adi", nullable = false)
    private String sirketAdi;

    @Column(name = "web_sitesi")
    private String webSitesi;

    // --- KRİTİK DEĞİŞİKLİK BURADA ---
    // @JsonIgnore ekledik. Bu sayede Mobili tıkayan ham veri (byte[]) gitmeyecek.
    // Ama aşağıda getLogoBase64() olduğu için resim hem Web'de hem Mobilde görünmeye devam edecek.
    @Lob 
    @Column(columnDefinition = "MEDIUMBLOB") 
    @JsonIgnore 
    private byte[] logo;

    @Column(columnDefinition = "TEXT")
    private String hakkimizda;
    
    private String sehir;
    private String adres;
    private Double enlem; 
    private Double boylam;

    // --- SONSUZ DÖNGÜ ENGELLEYİCİ ---
    @OneToMany(mappedBy = "isveren", cascade = CascadeType.ALL)
    @JsonIgnore 
    private List<IsIlani> ilanlar;

    // --- GETTER VE SETTER METODLARI ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Kullanici getKullanici() { return kullanici; }
    public void setKullanici(Kullanici kullanici) { this.kullanici = kullanici; }

    public String getSirketAdi() { return sirketAdi; }
    public void setSirketAdi(String sirketAdi) { this.sirketAdi = sirketAdi; }

    public String getWebSitesi() { return webSitesi; }
    public void setWebSitesi(String webSitesi) { this.webSitesi = webSitesi; }

    public byte[] getLogo() { return logo; }
    public void setLogo(byte[] logo) { this.logo = logo; }

    public String getHakkimizda() { return hakkimizda; }
    public void setHakkimizda(String hakkimizda) { this.hakkimizda = hakkimizda; }

    public String getSehir() { return sehir; }
    public void setSehir(String sehir) { this.sehir = sehir; }

    public String getAdres() { return adres; }
    public void setAdres(String adres) { this.adres = adres; }

    public Double getEnlem() { return enlem; }
    public void setEnlem(Double enlem) { this.enlem = enlem; }

    public Double getBoylam() { return boylam; }
    public void setBoylam(Double boylam) { this.boylam = boylam; }

    public List<IsIlani> getIlanlar() { return ilanlar; }
    public void setIlanlar(List<IsIlani> ilanlar) { this.ilanlar = ilanlar; }

    // --- HTML ve Mobil İçin Base64 Çevirici ---
    // Bu metod "get" ile başladığı için JSON'a otomatik eklenir ("logoBase64": "...")
    // Yani resim verisi Mobile string olarak gider, Web'de de bu metod kullanıldığı için sorun çıkmaz.
    public String getLogoBase64() {
        if (logo == null || logo.length == 0) {
            return null;
        }
        return Base64.getEncoder().encodeToString(logo);
    }
}