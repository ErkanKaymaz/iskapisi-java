package com.isinolsun.kontrolculer;

import com.isinolsun.dto.MobilBasvuruDTO; // <-- BU IMPORTU EKLE
import com.isinolsun.servisler.BasvuruService;
import com.isinolsun.varliklar.Basvuru;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/basvurular")
public class BasvuruController {

    private final BasvuruService basvuruService;

    public BasvuruController(BasvuruService basvuruService) {
        this.basvuruService = basvuruService;
    }

    // BAŞVURU YAP (ORTAK)
    @PostMapping("/yap")
    public Basvuru basvuruYap(@RequestBody Basvuru basvuru) {
        return basvuruService.basvuruYap(basvuru);
    }

    // --- WEB TARAFI İÇİN (AYNEN KALDI - BOZULMADI) ---
    // Web burayı kullanmaya devam edecek, Entity dönecek.
    @GetMapping("/ilan/{ilanId}")
    public List<Basvuru> ilanaGelenBasvurular(@PathVariable Long ilanId) {
        return basvuruService.ilanaGelenBasvurular(ilanId);
    }

    // --- MOBİL TARAFI İÇİN (YENİ EKLENDİ) ---
    // Mobil burayı kullanacak, DTO (Tarih verisi olan) dönecek.
    @GetMapping("/mobil/ilan/{ilanId}")
    public List<MobilBasvuruDTO> getMobilIlanBasvurulari(@PathVariable Long ilanId) {
        return basvuruService.getMobilIlanBasvurulari(ilanId);
    }
    
    // Mobil Kullanıcı Başvuruları İçin (Eğer lazımsa)
    @GetMapping("/mobil/kullanici/{kullaniciId}")
    public List<MobilBasvuruDTO> getMobilKullaniciBasvurulari(@PathVariable Long kullaniciId) {
        return basvuruService.getMobilKullaniciBasvurulari(kullaniciId);
    }
}