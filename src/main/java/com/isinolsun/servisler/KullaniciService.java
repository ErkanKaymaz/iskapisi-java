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
    private final MailService mailService; // Final yaptık, constructor ile gelecek

    // Şifre kodlarını tutan geçici hafıza
    private static final Map<String, String> verificationCodes = new HashMap<>();

    // TEK VE TEMİZ CONSTRUCTOR (Kurucu Metot)
    // Spring Boot burayı görünce hem Repository'yi hem MailService'i otomatik doldurur.
    public KullaniciService(KullaniciRepository kullaniciRepository, MailService mailService) {
        this.kullaniciRepository = kullaniciRepository;
        this.mailService = mailService;
    }

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

    // --- ŞİFRE SIFIRLAMA METODLARI ---

    public boolean sifreSifirlamaKoduGonder(String email) {
        Kullanici kullanici = kullaniciRepository.findByEmail(email).orElse(null);
        if (kullanici == null) {
            return false;
        }

        String kod = String.valueOf((int) (Math.random() * 900000) + 100000);
        verificationCodes.put(email, kod);

        // Artık mailService null gelmeyecek!
        try {
            mailService.mailGonder(email, "Şifre Sıfırlama Kodu", "Kodunuz: " + kod);
        } catch (Exception e) {
            // Mail servisi kapalıysa bile sistem çökmesin
            System.err.println("Mail hatası: " + e.getMessage());
        }
        
        return true;
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
