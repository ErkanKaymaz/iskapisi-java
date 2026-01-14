package com.isinolsun.varliklar;

import com.isinolsun.tipler.BasvuruDurumu;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "basvurular", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"is_ilani_id", "is_arayan_id"})
})
public class Basvuru {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "is_ilani_id", nullable = false)
    private IsIlani isIlani;

    @ManyToOne
    @JoinColumn(name = "is_arayan_id", nullable = false)
    private IsArayan isArayan;

    private LocalDateTime basvuruTarihi;

    // --- GÜNCELLENEN KISIM ---
    @Enumerated(EnumType.STRING)
    private BasvuruDurumu durum = BasvuruDurumu.BEKLEMEDE;

    private LocalDateTime mulakatTarihi; // Mülakat zamanı

    @Column(length = 1000)
    private String isverenNotu; // Adaya gösterilecek mesaj
    // -------------------------

    @PrePersist
    protected void onCreate() {
        basvuruTarihi = LocalDateTime.now();
    }

    // --- GETTER VE SETTERLAR ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public IsIlani getIsIlani() { return isIlani; }
    public void setIsIlani(IsIlani isIlani) { this.isIlani = isIlani; }

    public IsArayan getIsArayan() { return isArayan; }
    public void setIsArayan(IsArayan isArayan) { this.isArayan = isArayan; }

    public LocalDateTime getBasvuruTarihi() { return basvuruTarihi; }
    public void setBasvuruTarihi(LocalDateTime basvuruTarihi) { this.basvuruTarihi = basvuruTarihi; }

    public BasvuruDurumu getDurum() { return durum; }
    public void setDurum(BasvuruDurumu durum) { this.durum = durum; }
    
    // Yeni Alanlar
    public LocalDateTime getMulakatTarihi() { return mulakatTarihi; }
    public void setMulakatTarihi(LocalDateTime mulakatTarihi) { this.mulakatTarihi = mulakatTarihi; }

    public String getIsverenNotu() { return isverenNotu; }
    public void setIsverenNotu(String isverenNotu) { this.isverenNotu = isverenNotu; }
}