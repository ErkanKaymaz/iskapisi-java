package com.isinolsun.servisler;

import com.isinolsun.depolar.KullaniciRepository;
import com.isinolsun.varliklar.Kullanici;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class KullaniciService {

    private final KullaniciRepository kullaniciRepository;
    private final MailService mailService;

    // 1. KODLARI TUTAN HARİTA (Eski hali)
    private static final Map<String, String> verificationCodes = new HashMap<>();
    
    // 2. YENİ EKLENEN: KODUN OLUŞTURULMA ZAMANINI TUTAN HARİTA 🕒
    private static final Map<String, Long> verificationTimes = new HashMap<>();

    public KullaniciService(KullaniciRepository kullaniciRepository, MailService mailService) {
        this.kullaniciRepository = kullaniciRepository;
        this.mailService = mailService;
    }

    // ... Diğer metodlar (kaydet, girisYap vs.) aynı kalıyor ...

    // --- ŞİFRE KODU GÖNDERME ---
    public boolean sifreSifirlamaKoduGonder(String email) {
        Kullanici kullanici = kullaniciRepository.findByEmail(email).orElse(null);
        if (kullanici == null) {
            return false;
        }

        String kod = String.valueOf((int) (Math.random() * 900000) + 100000);
        
        // Kodu kaydet
        verificationCodes.put(email, kod);
        
        // YENİ: Şu anki zamanı (milisaniye cinsinden) kaydet 🕒
        verificationTimes.put(email, System.currentTimeMillis());

        try {
            mailService.mailGonder(email, "Şifre Sıfırlama Kodu", "Kodunuz: " + kod);
            return true; 
        } catch (Exception e) {
            System.err.println("Mail hatası: " + e.getMessage());
            return false;
        }
    }

    // --- ŞİFRE DEĞİŞTİRME ---
    public boolean sifreDegistir(String email, String girilenKod, String yeniSifre) {
        String gercekKod = verificationCodes.get(email);
        
        // YENİ: Kayıt zamanını al (Yoksa 0 döner) 🕒
        Long kayitZamani = verificationTimes.getOrDefault(email, 0L);
        long suAn = System.currentTimeMillis();
        
        // YENİ: 15 Dakika Kontrolü (15 * 60 * 1000 = 900.000 ms) ⏳
        // Eğer aradaki fark 15 dakikadan büyükse REDDET.
        if ((suAn - kayitZamani) > (15 * 60 * 1000)) {
            System.out.println("❌ Kodun süresi dolmuş: " + email);
            verificationCodes.remove(email); // Eski kodu temizle
            verificationTimes.remove(email); // Eski zamanı temizle
            return false;
        }

        if (gercekKod != null && gercekKod.equals(girilenKod)) {
            Kullanici k = kullaniciRepository.findByEmail(email).orElse(null);
            if (k != null) {
                k.setSifre(yeniSifre); 
                kullaniciRepository.save(k);
                
                // İşlem bitince hafızayı temizle
                verificationCodes.remove(email);
                verificationTimes.remove(email);
                return true;
            }
        }
        return false; 
    }
}
