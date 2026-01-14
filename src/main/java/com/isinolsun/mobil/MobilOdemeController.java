package com.isinolsun.mobil;

// DİKKAT: Senin projendeki doğru isimler bunlar
import com.isinolsun.depolar.IsIlaniRepository; 
import com.isinolsun.depolar.KullaniciRepository;
import com.isinolsun.depolar.PaketRepository;
import com.isinolsun.servisler.IyzicoService;
import com.isinolsun.varliklar.IsIlani; // <-- Düzeldi
import com.isinolsun.varliklar.Kullanici;
import com.isinolsun.varliklar.Paket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/mobil/odeme")
public class MobilOdemeController {

    @Autowired private PaketRepository paketRepository;
    @Autowired private IyzicoService iyzicoService;
    @Autowired private KullaniciRepository kullaniciRepository;
    
    // İŞTE HATAYI ÇÖZEN SATIR: İsmi düzelttik
    @Autowired private IsIlaniRepository isIlaniRepository; 

    // 1. PAKETLERİ GETİR
    @GetMapping("/paketler")
    public ResponseEntity<List<Paket>> getPaketler() {
        return ResponseEntity.ok(paketRepository.findAll());
    }

    // 2. SATIN AL (Iyzico ile ödeme - Web ile aynı)
    @PostMapping("/satin-al")
    public ResponseEntity<?> satinAl(@RequestBody Map<String, Object> istek) {
        try {
            Long ilanId = Long.valueOf(istek.get("ilanId").toString());
            Long paketId = Long.valueOf(istek.get("paketId").toString());
            
            // Kart Bilgileri (Web'deki gibi)
            String kartSahibi = (String) istek.get("kartSahibi");
            String kartNo = (String) istek.get("kartNo");
            String ay = (String) istek.get("ay");
            String yil = (String) istek.get("yil");
            String cvv = (String) istek.get("cvv");

            // Validasyon
            if (kartSahibi == null || kartNo == null || ay == null || yil == null || cvv == null) {
                return ResponseEntity.badRequest().body("Kart bilgileri eksik.");
            }

            // İlanı bul
            Optional<IsIlani> ilanOpt = isIlaniRepository.findById(ilanId);
            if (ilanOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("İlan bulunamadı.");
            }
            
            IsIlani ilan = ilanOpt.get();
            // İlanın sahibini (İşvereni) bul
            Kullanici kullanici = ilan.getIsveren().getKullanici();
            
            Optional<Paket> paketOpt = paketRepository.findById(paketId);
            if (paketOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Paket bulunamadı.");
            }
            Paket paket = paketOpt.get();

            // IYZICO ÖDEME SERVİSİNİ ÇAĞIR (Web ile aynı)
            boolean sonuc = iyzicoService.odemeYap(
                kullanici, 
                paket.getFiyat(), 
                kartSahibi, kartNo, ay, yil, cvv
            );

            if (sonuc) {
                // Ödeme başarılı - Vitrin tarihini güncelle (Web ile aynı mantık)
                java.time.LocalDateTime baslangic = java.time.LocalDateTime.now();
                
                // Eğer zaten vitrindeyse ve süresi bitmemişse, kaldığı yerden ekle
                if (ilan.isVitrinde() && ilan.getVitrinBitisTarihi() != null) {
                    if (ilan.getVitrinBitisTarihi().isAfter(baslangic)) {
                        baslangic = ilan.getVitrinBitisTarihi();
                    }
                }
                
                ilan.setVitrinBitisTarihi(baslangic.plusDays(paket.getGun()));
                isIlaniRepository.save(ilan);
                
                return ResponseEntity.ok("Ödeme Başarılı! İlan vitrine alındı. 🚀");
            } else {
                return ResponseEntity.badRequest().body("Ödeme başarısız oldu. Lütfen kart bilgilerinizi kontrol edin.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Hata: " + e.getMessage());
        }
    }
}