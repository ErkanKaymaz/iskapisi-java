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

    // Sadece kodları tutan basit harita (Zaman kontrolünü kaldırdık)
    private static final Map<String, String> verificationCodes = new HashMap<>();

    public KullaniciService(KullaniciRepository kullaniciRepository, MailService mailService) {
        this.kullaniciRepository = kullaniciRepository;
        this.mailService = mailService;
    }

    // --- TEMEL METODLAR (Bunlar silindiği için hata alıyordun) ---
    
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

    // --- ŞİFRE İŞLEMLERİ (Sadeleştirildi) ---

    public boolean sifreSifirlamaKoduGonder(String email) {
        Kullanici kullanici = kullaniciRepository.findByEmail(email).orElse(null);
        
        // Kullanıcı yoksa işlem yapma
        if (kullanici == null) {
            return false; 
        }

        String kod = String.valueOf((int) (Math.random() * 900000) + 100000);
        verificationCodes.put(email, kod);

        try {
            mailService.mailGonder(email, "Şifre Sıfırlama Kodu", "Kodunuz: " + kod);
            return true; 
        } catch (Exception e) {
            System.err.println("Mail hatası: " + e.getMessage());
            return false;
        }
    }

    public boolean sifreDegistir(String email, String girilenKod, String yeniSifre) {
        String gercekKod = verificationCodes.get(email);
        
        if (gercekKod != null && gercekKod.equals(girilenKod)) {
            Kullanici k = kullaniciRepository.findByEmail(email).orElse(null);
            if (k != null) {
                k.setSifre(yeniSifre); 
                kullaniciRepository.save(k);
                verificationCodes.remove(email);
                return true;
            }
        }
        return false; 
    }
}
