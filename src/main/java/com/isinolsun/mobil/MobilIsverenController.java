package com.isinolsun.mobil;

import com.isinolsun.depolar.IsIlaniRepository;
import com.isinolsun.depolar.IsverenRepository;
import com.isinolsun.servisler.IsIlaniService;
import com.isinolsun.varliklar.IsIlani;
import com.isinolsun.varliklar.Isveren;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime; // DÜZELTME: LocalDate yerine LocalDateTime
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/mobil/ilan")
public class MobilIsverenController {

    @Autowired
    private IsIlaniRepository isIlaniRepository;
    @Autowired
    private IsverenRepository isverenRepository;
    @Autowired
    private IsIlaniService isIlaniService;

    // 1. İLAN EKLE
    @PostMapping("/ekle")
    public ResponseEntity<?> ilanEkle(@RequestBody Map<String, Object> veri) {
        try {
            // Gelen verileri al
            Long kullaniciId = Long.valueOf(veri.get("isverenId").toString());
            String baslik = (String) veri.get("baslik");
            String sehir = (String) veri.get("sehir");
            String maas = (String) veri.get("maas");
            String aciklama = (String) veri.get("aciklama");

            // Kullanıcı ID'den İşvereni bul
            Optional<Isveren> isverenOpt = isverenRepository.findByKullaniciId(kullaniciId);
            if (isverenOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("İşveren profili bulunamadı.");
            }

            IsIlani yeniIlan = new IsIlani();
            yeniIlan.setBaslik(baslik);
            yeniIlan.setSehir(sehir);
            yeniIlan.setMaas(maas);
            yeniIlan.setAciklama(aciklama);
            
            // --- DÜZELTME BURADA YAPILDI ---
            // LocalDate.now() yerine LocalDateTime.now() kullandık.
            yeniIlan.setYayinlanmaTarihi(LocalDateTime.now()); 
            
            yeniIlan.setAktif(true);
            yeniIlan.setIsveren(isverenOpt.get());

            isIlaniRepository.save(yeniIlan);

            return ResponseEntity.ok("İlan başarıyla yayınlandı.");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Hata: " + e.getMessage());
        }
    }
 // 4. İLAN DÜZENLE / GÜNCELLE
    @PostMapping("/duzenle")
    public ResponseEntity<?> ilanDuzenle(@RequestBody Map<String, Object> veri) {
        try {
            Long ilanId = Long.valueOf(veri.get("id").toString());
            String baslik = (String) veri.get("baslik");
            String sehir = (String) veri.get("sehir");
            String maas = (String) veri.get("maas");
            String aciklama = (String) veri.get("aciklama");

            Optional<IsIlani> ilanOpt = isIlaniRepository.findById(ilanId);
            if (ilanOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("İlan bulunamadı.");
            }

            IsIlani ilan = ilanOpt.get();
            ilan.setBaslik(baslik);
            ilan.setSehir(sehir);
            ilan.setMaas(maas);
            ilan.setAciklama(aciklama);
            // Tarihi güncellemiyoruz ki "yeni ilan" gibi en üste çıkmasın (istersen güncelleyebilirsin)
            
            isIlaniRepository.save(ilan);

            return ResponseEntity.ok("İlan güncellendi.");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Güncelleme hatası: " + e.getMessage());
        }
    }
    // 2. İŞVERENİN İLANLARINI LİSTELE
    @GetMapping("/listele/{kullaniciId}")
    public ResponseEntity<?> ilanlarimiListele(@PathVariable Long kullaniciId) {
        Optional<Isveren> isverenOpt = isverenRepository.findByKullaniciId(kullaniciId);
        if (isverenOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("İşveren bulunamadı.");
        }

        List<IsIlani> ilanlar = isIlaniRepository.findByIsverenId(isverenOpt.get().getId());
        return ResponseEntity.ok(ilanlar);
    }

    // 3. İLAN SİL
    @PostMapping("/sil/{ilanId}")
    public ResponseEntity<?> ilanSil(@PathVariable Long ilanId) {
        try {
            isIlaniRepository.deleteById(ilanId);
            return ResponseEntity.ok("İlan silindi.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Silme hatası: " + e.getMessage());
        }
    }

    // 5. İLAN TEKRAR YAYINLA (Web ile uyumlu)
    @PostMapping("/tekrar-yayinla/{ilanId}")
    public ResponseEntity<?> ilanTekrarYayinla(@PathVariable Long ilanId) {
        try {
            Optional<IsIlani> ilanOpt = isIlaniRepository.findById(ilanId);
            if (ilanOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("İlan bulunamadı.");
            }
            
            IsIlani ilan = ilanOpt.get();
            ilan.setAktif(true);
            isIlaniRepository.save(ilan);
            
            return ResponseEntity.ok("İlan tekrar yayınlandı.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Hata: " + e.getMessage());
        }
    }
}