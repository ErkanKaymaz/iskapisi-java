package com.isinolsun.kontrolculer;

import com.isinolsun.servisler.KullaniciService;
import com.isinolsun.varliklar.Kullanici;
import org.springframework.web.bind.annotation.*;

@RestController // Bu sınıfın bir web API olduğunu belirtir (JSON döner).
@RequestMapping("/api/kullanicilar") // Adresimiz: localhost:8080/api/kullanicilar
public class KullaniciController {

    private final KullaniciService kullaniciService;

    // Manuel Constructor (Lombok olmadığı için)
    public KullaniciController(KullaniciService kullaniciService) {
        this.kullaniciService = kullaniciService;
    }

    // KAYIT OLMA: POST isteği ile çalışır
    @PostMapping("/kayit")
    public Kullanici kayitOl(@RequestBody Kullanici kullanici) {
        return kullaniciService.kullaniciKaydet(kullanici);
    }

    // GİRİŞ YAPMA: E-posta ve şifre kontrolü
    @PostMapping("/giris")
    public Kullanici girisYap(@RequestParam String email, @RequestParam String sifre) {
        return kullaniciService.girisYap(email, sifre);
    }
}