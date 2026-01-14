package com.isinolsun.mobil;

import com.isinolsun.depolar.IsArayanRepository;
import com.isinolsun.depolar.IsverenRepository;
import com.isinolsun.servisler.GeminiService;
import com.isinolsun.varliklar.IsArayan;
import com.isinolsun.varliklar.Isveren;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/mobil/ai")
@CrossOrigin(origins = "*") 
public class MobilAIController {

    @Autowired
    private GeminiService geminiService;
    @Autowired
    private IsArayanRepository isArayanRepository;
    @Autowired
    private IsverenRepository isverenRepository;

    // --- 1. GENEL SOHBET (YENİ EKLENEN KISIM) ---
    @PostMapping("/sohbet")
    public ResponseEntity<?> genelSohbet(@RequestBody Map<String, String> veri) {
        try {
            String mesaj = veri.get("mesaj");
            if (mesaj == null || mesaj.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Mesaj boş olamaz.");
            }
            
            // Gemini Servisine sor (Kısa ve öz cevap vermesi için promptu serviste ayarlamıştık)
            String cevap = geminiService.yapayZekayaSor(mesaj);
            
            // Cevabı JSON olarak dönüyoruz: { "cevap": "..." }
            return ResponseEntity.ok(Map.of("cevap", cevap));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("AI Hatası: " + e.getMessage());
        }
    }

    // 2. İLAN METNİ OLUŞTURUCU
    @PostMapping("/ilan-olustur")
    public ResponseEntity<?> ilanMetniOlustur(@RequestBody Map<String, String> veri) {
        try {
            Long kullaniciId = Long.valueOf(veri.get("kullaniciId"));
            String baslik = veri.get("baslik");

            if (baslik == null || baslik.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("İlan başlığı boş olamaz.");
            }

            Optional<Isveren> isverenOpt = isverenRepository.findByKullaniciId(kullaniciId);
            String sirketAdi = "Şirketimiz";
            if (isverenOpt.isPresent()) {
                sirketAdi = isverenOpt.get().getSirketAdi();
            }

            String ilanMetni = geminiService.ilanMetniOlusturMobil(baslik, sirketAdi);
            return ResponseEntity.ok(Map.of("ilanMetni", ilanMetni));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Hata: " + e.getMessage());
        }
    }

    // 3. CV ANALİZİ
    @GetMapping("/cv-analiz/{kullaniciId}")
    public ResponseEntity<?> cvAnalizi(@PathVariable Long kullaniciId) {
        try {
            Optional<IsArayan> isArayanOpt = isArayanRepository.findByKullaniciId(kullaniciId);
            if (isArayanOpt.isEmpty()) return ResponseEntity.badRequest().body("Profil yok.");
            
            IsArayan k = isArayanOpt.get();
            String analiz = geminiService.kariyerAnaliziOlustur(k.getMeslek(), k.getOzet_bilgi(), "");
            return ResponseEntity.ok(Map.of("analiz", analiz));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Hata: " + e.getMessage());
        }
    }
}