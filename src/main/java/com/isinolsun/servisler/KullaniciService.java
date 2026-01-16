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

    // Şifre kodlarını tutan harita
    private static final Map<String, String> verificationCodes = new HashMap<>();
    
    // Kodun oluşturulma zamanını tutan harita (15 dk kontrolü için)
    private static final Map<String, Long> verificationTimes = new HashMap<>();

    public KullaniciService(KullaniciRepository kullaniciRepository, MailService mailService) {
        this.kullaniciRepository = kullaniciRepository;
        this.mailService = mailService;
    }

    // --- SİLİNEN METODLAR GERİ GELDİ ---
    
    public Kullanici kullaniciKaydet(Kullanici kullanici) {
        return kullaniciRepository.save(kullanici);
    }

    public Kullanici girisYap(String email, String sifre) {
        Optional<Kullanici> k = kullaniciRepository.findByEmail(email);
        if (k.isPresent() && k.get().getSifre().equals(sifre)) {
            return k.get();
        }
        return null;
    }
    
    public void kullaniciGuncelle(Kullanici kullanici) {
        kullaniciRepository.save(kullanici);
    }
    
    // ------------------------------------

    // --- ŞİFRE SIFIRLAMA METODLARI ---

    public boolean sifreSifirlamaKoduGonder(String email) {
        // 1. Kullanıcıyı bul
        Kullanici kullanici = kullaniciRepository.findByEmail(email).orElse(null);
        
        // Kullanıcı yoksa hemen FALSE dön
        if (kullanici == null) {
            return false; 
        }

        String kod = String.valueOf((int) (Math.random() * 900000) + 100000);
        
        // Kodu ve zamanı kaydet
        verificationCodes.put(email, kod);
        verificationTimes.put(email, System.currentTimeMillis());

        try {
            mailService.mailGonder(email, "Şifre Sıfırlama Kodu", "Kodunuz: " + kod);
            // Mail gerçekten gittiyse TRUE dön
            return true; 
        } catch (Exception e) {
            System.err.println("Mail hatası: " + e.getMessage());
            // Mail hatası varsa FALSE dön
            return false;
        }
    }

    public boolean sifreDegistir(String email, String girilenKod, String yeniSifre) {
        String gercekKod = verificationCodes.get(email);
        
        // Zaman kontrolü (Varsayılan 0)
        Long kayitZamani = verificationTimes.getOrDefault(email, 0L);
        long suAn = System.currentTimeMillis();
        
        // 15 Dakika (900.000 ms) kontrolü
        if ((suAn - kayitZamani) > (15 * 60 * 1000)) {
            // Süre dolmuşsa temizle ve reddet
            verificationCodes.remove(email);
            verificationTimes.remove(email);
            return false;
        }

        if (gercekKod != null && gercekKod.equals(girilenKod)) {
            Kullanici k = kullaniciRepository.findByEmail(email).orElse(null);
            if (k != null) {
                k.setSifre(yeniSifre); 
                kullaniciRepository.save(k);
                
                // İşlem başarılı, hafızayı temizle
                verificationCodes.remove(email);
                verificationTimes.remove(email);
                return true;
            }
        }
        return false; 
    }
}
