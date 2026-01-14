package com.isinolsun.kontrolculer;

import com.isinolsun.servisler.IsIlaniService;
import com.isinolsun.varliklar.IsIlani;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ilanlar")
public class IsIlaniController {

    private final IsIlaniService isIlaniService;

    public IsIlaniController(IsIlaniService isIlaniService) {
        this.isIlaniService = isIlaniService;
    }

    // TÜM İLANLARI GETİR
    @GetMapping
    public List<IsIlani> tumIlanlar() {
        return isIlaniService.tumAktifIlanlariGetir();
    }

    // YENİ İLAN EKLE
    @PostMapping("/ekle")
    public IsIlani ilanEkle(@RequestBody IsIlani isIlani) {
        return isIlaniService.ilanKaydet(isIlani);
    }

    // ŞEHRE GÖRE ARA
    // Örnek: /api/ilanlar/ara-sehir?sehir=Istanbul
    @GetMapping("/ara-sehir")
    public List<IsIlani> sehreGoreAra(@RequestParam String sehir) {
        return isIlaniService.sehreGoreAra(sehir);
    }

    // BAŞLIĞA GÖRE ARA
    // Örnek: /api/ilanlar/ara-baslik?baslik=Garson
    @GetMapping("/ara-baslik")
    public List<IsIlani> basligaGoreAra(@RequestParam String baslik) {
        return isIlaniService.basligaGoreAra(baslik);
    }
    
    // TEK BİR İLAN DETAYI
    @GetMapping("/{id}")
    public IsIlani ilanDetay(@PathVariable Long id) {
        return isIlaniService.ilanGetir(id);
    }
}