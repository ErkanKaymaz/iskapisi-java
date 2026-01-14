package com.isinolsun.varliklar;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore; // BU IMPORT ŞART

@Entity
@Table(name = "is_ilanlari")
public class IsIlani {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "isveren_id", nullable = false)
    private Isveren isveren;

    @Column(nullable = false)
    private String baslik;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String aciklama;

    @Column(nullable = false)
    private String sehir;

    private String maas;
    private LocalDateTime yayinlanmaTarihi;
    private LocalDate sonBasvuruTarihi;
    private boolean aktif = true;
    
    // Vitrin (Öne Çıkarma) Tarihi
    private LocalDateTime vitrinBitisTarihi;

    // --- KRİTİK NOKTA BURASI ---
    // Web tarafında bu listeye ihtiyacın var (Başvuruları listelemek için).
    // Ama Mobilde ilanları çekerken bu liste JSON'a girerse sonsuz döngü olur.
    // ÇÖZÜM: @JsonIgnore ekliyoruz. Web çalışır, Mobil patlamaz.
    @OneToMany(mappedBy = "isIlani", cascade = CascadeType.ALL)
    @JsonIgnore 
    private List<Basvuru> basvurular;

    @PrePersist
    protected void onCreate() {
        yayinlanmaTarihi = LocalDateTime.now();
    }

    // --- GETTER VE SETTERLAR ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Isveren getIsveren() { return isveren; }
    public void setIsveren(Isveren isveren) { this.isveren = isveren; }

    public String getBaslik() { return baslik; }
    public void setBaslik(String baslik) { this.baslik = baslik; }

    public String getAciklama() { return aciklama; }
    public void setAciklama(String aciklama) { this.aciklama = aciklama; }

    public String getSehir() { return sehir; }
    public void setSehir(String sehir) { this.sehir = sehir; }

    public String getMaas() { return maas; }
    public void setMaas(String maas) { this.maas = maas; }

    public LocalDateTime getYayinlanmaTarihi() { return yayinlanmaTarihi; }
    public void setYayinlanmaTarihi(LocalDateTime yayinlanmaTarihi) { this.yayinlanmaTarihi = yayinlanmaTarihi; }

    public LocalDate getSonBasvuruTarihi() { return sonBasvuruTarihi; }
    public void setSonBasvuruTarihi(LocalDate sonBasvuruTarihi) { this.sonBasvuruTarihi = sonBasvuruTarihi; }

    public boolean isAktif() { return aktif; }
    public void setAktif(boolean aktif) { this.aktif = aktif; }

    public LocalDateTime getVitrinBitisTarihi() { return vitrinBitisTarihi; }
    public void setVitrinBitisTarihi(LocalDateTime vitrinBitisTarihi) { this.vitrinBitisTarihi = vitrinBitisTarihi; }

    public List<Basvuru> getBasvurular() { return basvurular; }
    public void setBasvurular(List<Basvuru> basvurular) { this.basvurular = basvurular; }

    // Yardımcı metod: Şu an vitrinde mi?
    // (JSON'a "vitrinde": true/false diye otomatik gider)
    public boolean isVitrinde() {
        return vitrinBitisTarihi != null && vitrinBitisTarihi.isAfter(LocalDateTime.now());
    }
}