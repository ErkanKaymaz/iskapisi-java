package com.isinolsun.mobil;

import com.isinolsun.depolar.IsArayanRepository;
import com.isinolsun.depolar.IsIlaniRepository;
import com.isinolsun.dto.MobilIlanDTO;
import com.isinolsun.servisler.FavoriService;
import com.isinolsun.servisler.IsIlaniService;
import com.isinolsun.varliklar.Favori;
import com.isinolsun.varliklar.IsArayan;
import com.isinolsun.varliklar.IsIlani;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mobil/favori")
@CrossOrigin(origins = "*")
public class MobilFavoriController {

    @Autowired
    private FavoriService favoriService;
    @Autowired
    private IsArayanRepository isArayanRepository;
    @Autowired
    private IsIlaniService isIlaniService;

    // FAVORİ DURUMU KONTROL ET
    @GetMapping("/kontrol")
    public ResponseEntity<Boolean> favoriMi(@RequestParam Long kullaniciId, @RequestParam Long ilanId) {
        Optional<IsArayan> isArayanOpt = isArayanRepository.findByKullaniciId(kullaniciId);
        if (isArayanOpt.isEmpty()) return ResponseEntity.ok(false);
        
        boolean durum = favoriService.favoriMi(isArayanOpt.get().getId(), ilanId);
        return ResponseEntity.ok(durum);
    }

    // FAVORİ İŞLEMİ (Ekle/Çıkar)
    @PostMapping("/islem")
    public ResponseEntity<String> favoriIslem(@RequestBody java.util.Map<String, Long> veri) {
        Long kullaniciId = veri.get("kullaniciId");
        Long ilanId = veri.get("ilanId");

        Optional<IsArayan> isArayanOpt = isArayanRepository.findByKullaniciId(kullaniciId);
        if (isArayanOpt.isEmpty()) return ResponseEntity.badRequest().body("İş arayan profili bulunamadı.");

        IsIlani ilan = isIlaniService.ilanGetir(ilanId);
        if (ilan == null) return ResponseEntity.badRequest().body("İlan bulunamadı.");

        favoriService.favoriIslemi(isArayanOpt.get(), ilan);

        boolean artikFavoriMi = favoriService.favoriMi(isArayanOpt.get().getId(), ilanId);
        return ResponseEntity.ok(artikFavoriMi ? "Favorilere Eklendi ❤️" : "Favorilerden Çıkarıldı 💔");
    }

    // --- FAVORİLERİMİ LİSTELE (DÜZELTİLDİ) ---
    // PathVariable yerine RequestParam yaptık. URL artık "/listele?kullaniciId=..." formatını kabul eder.
    @GetMapping("/listele") 
    public ResponseEntity<List<MobilIlanDTO>> favorileriGetir(@RequestParam Long kullaniciId) {
        Optional<IsArayan> isArayanOpt = isArayanRepository.findByKullaniciId(kullaniciId);
        if (isArayanOpt.isEmpty()) return ResponseEntity.badRequest().build();

        List<Favori> favoriler = favoriService.getKullaniciFavorileri(isArayanOpt.get().getId());

        List<MobilIlanDTO> temizListe = favoriler.stream().map(f -> {
             IsIlani ilan = f.getIsIlani();
             byte[] logoBytes = null;
             String sirketAdi = "Belirtilmemiş";
             Double enlem = null;
             Double boylam = null;

             if(ilan.getIsveren() != null) {
                 logoBytes = ilan.getIsveren().getLogo();
                 sirketAdi = ilan.getIsveren().getSirketAdi();
                 enlem = ilan.getIsveren().getEnlem();
                 boylam = ilan.getIsveren().getBoylam();
             }
             
             return new MobilIlanDTO(
                ilan.getId(),
                ilan.getBaslik(),
                sirketAdi,
                ilan.getSehir(),
                ilan.getMaas(),
                ilan.getYayinlanmaTarihi(),
                logoBytes, 
                ilan.getAciklama(),
                enlem,
                boylam,
                ilan.getVitrinBitisTarihi()
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(temizListe);
    }
}