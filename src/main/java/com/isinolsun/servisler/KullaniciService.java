package com.isinolsun.servisler;

import com.isinolsun.depolar.KullaniciRepository;
import com.isinolsun.varliklar.Kullanici;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class KullaniciService {

    private final KullaniciRepository kullaniciRepository;

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
}