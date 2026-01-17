package com.isinolsun.servisler;

import com.isinolsun.depolar.KullaniciRepository;
import com.isinolsun.varliklar.Kullanici;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class KullaniciService {

    private final KullaniciRepository kullaniciRepository;
    private static final Map<String, String> verificationCodes = new HashMap<>();
    // MANUEL CONSTRUCTOR EKLİYORUZ
    public KullaniciService(KullaniciRepository kullaniciRepository) {
        this.kullaniciRepository = kullaniciRepository;
    }

    public Kullanici kullaniciKaydet(Kullanici kullanici) {
        return kullaniciRepository.save(kullanici);
    }

    public Kullanici girisYap(String email, String sifre) {
        Optional<Kullanici> kullanici = kullaniciRepository.findByEmail(email);
        if (kullanici.isPresent() && kullanici.get().getSifre().equals(sifre)) {
            return kullanici.get();
        }
        return null;
    }
 // Kullanıcı bilgilerini güncelle (Sadece temel bilgiler)
    public void kullaniciGuncelle(Kullanici kullanici) {
        kullaniciRepository.save(kullanici);
    }
    @Autowired
    private MailService mailService;
    public boolean sifreSifirlamaKoduGonder(String email) {
        // 1. Kullanıcı var mı kontrol et
        Kullanici kullanici = kullaniciRepository.findByEmail(email).orElse(null);
        if (kullanici == null) {
            return false; // Böyle biri yok
        }

        // 2. 6 haneli rastgele kod üret
        String kod = String.valueOf((int) (Math.random() * 900000) + 100000);
        
        // 3. Kodu hafızaya kaydet (Mail -> Kod eşleşmesi)
        verificationCodes.put(email, kod);

        // 4. Python Servisi ile Mail At
        mailService.mailGonder(email, "Şifre Sıfırlama Kodu", "Merhaba, şifre sıfırlama kodun: " + kod);
        
        return true;
    }

    // ŞİFRE DEĞİŞTİRME METODU
    public boolean sifreDegistir(String email, String girilenKod, String yeniSifre) {
        // 1. Kod doğru mu?
        String gercekKod = verificationCodes.get(email);
        
        if (gercekKod != null && gercekKod.equals(girilenKod)) {
            // Kod doğru! Kullanıcıyı bul ve şifresini güncelle
            Kullanici k = kullaniciRepository.findByEmail(email).orElse(null);
            if (k != null) {
                // Şifreyi güncelle (Normalde hashlemek gerekir ama şimdilik düz yapıyoruz)
                k.setSifre(yeniSifre); 
                kullaniciRepository.save(k);
                
                // Kodu hafızadan sil (Tek kullanımlık olsun)
                verificationCodes.remove(email);
                return true;
            }
        }
        return false; // Kod yanlış veya süre dolmuş
    }
}
