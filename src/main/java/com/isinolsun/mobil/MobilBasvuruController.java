package com.isinolsun.mobil;

import com.isinolsun.depolar.BasvuruRepository;
import com.isinolsun.depolar.IsArayanRepository;
import com.isinolsun.depolar.IsIlaniRepository;
import com.isinolsun.dto.MobilBasvuruDTO; // <-- DTO IMPORT ETMEYİ UNUTMA
import com.isinolsun.servisler.BasvuruService;
import com.isinolsun.servisler.IsIlaniService;
import com.isinolsun.tipler.BasvuruDurumu;
import com.isinolsun.varliklar.Basvuru;
import com.isinolsun.varliklar.IsArayan;
import com.isinolsun.varliklar.IsIlani;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/mobil/basvuru")
@CrossOrigin(origins = "*") // React Native erişimi için önemli
public class MobilBasvuruController {

    @Autowired
    private BasvuruService basvuruService;
    @Autowired
    private IsIlaniService isIlaniService;
    @Autowired
    private IsArayanRepository isArayanRepository;
    @Autowired
    private BasvuruRepository basvuruRepository;
    @Autowired
    private IsIlaniRepository isIlaniRepository;

    // 1. KONTROL ET
    @GetMapping("/kontrol")
    public ResponseEntity<Boolean> basvuruKontrol(@RequestParam Long kullaniciId, @RequestParam Long ilanId) {
        try {
            Optional<IsArayan> isArayanOpt = isArayanRepository.findByKullaniciId(kullaniciId);
            if (isArayanOpt.isEmpty()) return ResponseEntity.ok(false);

            boolean varMi = basvuruRepository.existsByIsIlaniIdAndIsArayanId(ilanId, isArayanOpt.get().getId());
            return ResponseEntity.ok(varMi);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }

    // 2. BAŞVURU YAP
    @PostMapping("/yap")
    public ResponseEntity<?> basvuruYap(@RequestBody Map<String, Long> veri) {
        try {
            Long kullaniciId = veri.get("kullaniciId");
            Long ilanId = veri.get("ilanId");

            Optional<IsIlani> ilanOpt = isIlaniRepository.findById(ilanId);
            if (ilanOpt.isEmpty()) return ResponseEntity.badRequest().body("İlan bulunamadı.");
            
            Optional<IsArayan> isArayanOpt = isArayanRepository.findByKullaniciId(kullaniciId);
            if(isArayanOpt.isEmpty()) return ResponseEntity.badRequest().body("Profil eksik.");

            IsArayan isArayan = isArayanOpt.get();

            if (basvuruRepository.existsByIsIlaniIdAndIsArayanId(ilanId, isArayan.getId())) {
                return ResponseEntity.badRequest().body("Bu ilana zaten başvurunuz var.");
            }

            Basvuru basvuru = new Basvuru();
            basvuru.setIsArayan(isArayan); 
            basvuru.setIsIlani(ilanOpt.get());
            basvuru.setBasvuruTarihi(LocalDateTime.now()); 
            basvuru.setDurum(BasvuruDurumu.BEKLEMEDE);

            basvuruRepository.save(basvuru);
            return ResponseEntity.ok("Başvuru alındı.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Hata: " + e.getMessage());
        }
    }

    // 3. İŞ ARAYANIN BAŞVURULARI (DTO Kullanarak - GÜNCELLENDİ)
    @GetMapping("/listele/{kullaniciId}")
    public ResponseEntity<List<MobilBasvuruDTO>> basvurulariGetir(@PathVariable Long kullaniciId) {
        // Artık servisteki yeni metod sayesinde DTO dönüyor
        return ResponseEntity.ok(basvuruService.getMobilKullaniciBasvurulari(kullaniciId));
    }

    // 4. BİR İLANA YAPILAN BAŞVURULARI GETİR (İşveren İçin - GÜNCELLENDİ)
    @GetMapping("/ilan/{ilanId}")
    public ResponseEntity<List<MobilBasvuruDTO>> ilanaYapilanBasvurulariGetir(@PathVariable Long ilanId) {
        // BURASI ARTIK DÜZELDİ: Servisten aday bilgilerini içeren DTO listesi alıyor
        return ResponseEntity.ok(basvuruService.getMobilIlanBasvurulari(ilanId));
    }

    // 5. BAŞVURU DURUMU GÜNCELLE
    @PostMapping("/durum-guncelle")
    public ResponseEntity<?> basvuruDurumGuncelle(@RequestBody Map<String, Object> veri) {
        try {
            Long basvuruId = Long.valueOf(veri.get("basvuruId").toString());
            String durumStr = (String) veri.get("durum"); 
            String mesaj = (String) veri.get("mesaj");
            String tarihStr = (String) veri.get("tarih"); // Mobilden gelen String tarih
            
            Boolean ilaniKapat = veri.get("ilaniKapat") != null ? Boolean.valueOf(veri.get("ilaniKapat").toString()) : false;

            Optional<Basvuru> basvuruOpt = basvuruRepository.findById(basvuruId);
            if (basvuruOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Başvuru bulunamadı.");
            }

            Basvuru basvuru = basvuruOpt.get();
            BasvuruDurumu yeniDurum = BasvuruDurumu.valueOf(durumStr);
            
            // Tarih Parse İşlemi
            LocalDateTime mulakatTarihi = null;
            if (tarihStr != null && !tarihStr.isEmpty()) {
                try {
                    // Türkçe formatı parse etmek için Formatter
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy HH:mm", new Locale("tr"));
                    mulakatTarihi = LocalDateTime.parse(tarihStr, formatter);
                } catch (Exception e) {
                    System.out.println("Tarih parse hatası (Özel Format deneniyor): " + e.getMessage());
                    // ISO formatı denemesi
                    try {
                        mulakatTarihi = LocalDateTime.parse(tarihStr);
                    } catch (Exception ex) {
                        System.out.println("Tarih formatı tamamen geçersiz: " + tarihStr);
                    }
                }
            }
            
            // Servis metodunu çağır
            basvuruService.durumGuncelle(basvuruId, yeniDurum, mesaj, mulakatTarihi);
            
            if (yeniDurum == BasvuruDurumu.KABUL_EDILDI && ilaniKapat) {
                IsIlani ilan = basvuru.getIsIlani();
                ilan.setAktif(false);
                isIlaniRepository.save(ilan);
            }
            
            return ResponseEntity.ok("Başvuru durumu başarıyla güncellendi.");
        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.badRequest().body("Hata: " + e.getMessage());
        }
    }
    
    // 6. CV GETİR
    @GetMapping("/cv-getir/{basvuruId}")
    public ResponseEntity<?> cvGetir(@PathVariable Long basvuruId) {
        Optional<Basvuru> basvuruOpt = basvuruRepository.findById(basvuruId);
        if (basvuruOpt.isEmpty() || basvuruOpt.get().getIsArayan().getCvDosya() == null) {
            return ResponseEntity.notFound().build();
        }
        byte[] cvData = basvuruOpt.get().getIsArayan().getCvDosya();
        String base64 = Base64.getEncoder().encodeToString(cvData);
        return ResponseEntity.ok(Map.of("cvBase64", base64));
    }
}