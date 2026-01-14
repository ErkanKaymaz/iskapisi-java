package com.isinolsun.mobil;

import com.isinolsun.depolar.*;
import com.isinolsun.servisler.BildirimService;
import com.isinolsun.servisler.IsIlaniService;
import com.isinolsun.tipler.KullaniciRolu;
import com.isinolsun.varliklar.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/mobil/admin")
public class MobilAdminController {

    @Autowired private KullaniciRepository kullaniciRepository;
    @Autowired private IsIlaniRepository isIlaniRepository;
    @Autowired private PaketRepository paketRepository;
    @Autowired private BildirimService bildirimService;
    @Autowired private BasvuruRepository basvuruRepository;
    @Autowired private FavoriRepository favoriRepository;
    @Autowired private IsArayanRepository isArayanRepository;
    @Autowired private IsverenRepository isverenRepository;
    @Autowired private IsIlaniService isIlaniService;
    
    // Bildirim repository'sini ekledik
    @Autowired private BildirimRepository bildirimRepository; 

    // --- 1. DASHBOARD İSTATİSTİKLERİ ---
    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboardVerileri() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("toplamKullanici", kullaniciRepository.count());
        stats.put("aktifIlan", isIlaniRepository.count());
        stats.put("isverenSayisi", kullaniciRepository.countByRol(KullaniciRolu.ISVEREN));
        stats.put("isArayanSayisi", kullaniciRepository.countByRol(KullaniciRolu.IS_ARAYAN));
        
        List<IsIlani> ilanlar = isIlaniRepository.findAll();
        Map<String, Integer> sehirIlanSayilari = new HashMap<>();
        for (IsIlani ilan : ilanlar) {
            sehirIlanSayilari.put(ilan.getSehir(), sehirIlanSayilari.getOrDefault(ilan.getSehir(), 0) + 1);
        }
        stats.put("sehirIstatistigi", sehirIlanSayilari);

        return ResponseEntity.ok(stats);
    }

    // --- 2. KULLANICI YÖNETİMİ ---
    @GetMapping("/kullanicilar")
    public ResponseEntity<List<Kullanici>> kullanicilariGetir() {
        return ResponseEntity.ok(kullaniciRepository.findAll());
    }

    // --- KULLANICI SİLME METODU ---
    @PostMapping("/kullanici-sil/{id}")
    public ResponseEntity<?> kullaniciSil(@PathVariable Long id) {
        Kullanici silinecekKullanici = kullaniciRepository.findById(id).orElse(null);
        if (silinecekKullanici == null) {
            return ResponseEntity.badRequest().body("Kullanıcı bulunamadı.");
        }

        try {
            // 1. İŞ ARAYAN İSE İLGİLİ VERİLERİ SİL
            if (silinecekKullanici.getRol() == KullaniciRolu.IS_ARAYAN) {
                Optional<IsArayan> isArayanOpt = isArayanRepository.findByKullaniciId(id);
                if (isArayanOpt.isPresent()) {
                    IsArayan isArayan = isArayanOpt.get();
                    
                    // Favorileri Sil
                    List<Favori> favoriler = favoriRepository.findByIsArayanIdOrderByEklenmeTarihiDesc(isArayan.getId());
                    if (favoriler != null && !favoriler.isEmpty()) {
                        favoriRepository.deleteAll(favoriler);
                    }
                    
                    // Başvuruları Sil
                    List<Basvuru> basvurular = basvuruRepository.findByIsArayanId(isArayan.getId());
                    if (basvurular != null && !basvurular.isEmpty()) {
                        basvuruRepository.deleteAll(basvurular);
                    }

                    // Profili Sil
                    isArayanRepository.delete(isArayan);
                }
            } 
            // 2. İŞVEREN İSE İLGİLİ VERİLERİ SİL
            else if (silinecekKullanici.getRol() == KullaniciRolu.ISVEREN) {
                Optional<Isveren> isverenOpt = isverenRepository.findByKullaniciId(id);
                if (isverenOpt.isPresent()) {
                    Isveren isveren = isverenOpt.get();
                    
                    // İlanlarını ve onlara bağlı başvuruları sil
                    List<IsIlani> ilanlar = isIlaniRepository.findByIsverenId(isveren.getId());
                    for (IsIlani ilan : ilanlar) {
                        isIlaniService.ilanSil(ilan.getId());
                    }
                    
                    // Profili Sil
                    isverenRepository.delete(isveren);
                }
            }

            // --- 3. BİLDİRİMLERİ SİL (DÜZELTİLDİ) ---
            // Senin Repository'de olan mevcut metodu kullanıyoruz: findByKullaniciIdOrderByTarihDesc
            List<Bildirim> kullaniciBildirimleri = bildirimRepository.findByKullaniciIdOrderByTarihDesc(id);
            
            if (kullaniciBildirimleri != null && !kullaniciBildirimleri.isEmpty()) {
                bildirimRepository.deleteAll(kullaniciBildirimleri);
            }

            // 4. Ana Kullanıcıyı Sil
            kullaniciRepository.deleteById(id);
            return ResponseEntity.ok("Kullanıcı ve tüm verileri başarıyla silindi.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Silme işlemi başarısız: " + e.getMessage());
        }
    }
    // ---------------------------------------------

    // --- 3. İLAN YÖNETİMİ ---
    @GetMapping("/ilanlar")
    public ResponseEntity<List<IsIlani>> tumIlanlariGetir() {
        return ResponseEntity.ok(isIlaniRepository.findAll());
    }

    @PostMapping("/ilan-sil/{id}")
    public ResponseEntity<?> ilanSil(@PathVariable Long id, @RequestBody Map<String, String> veri) {
        IsIlani ilan = isIlaniRepository.findById(id).orElse(null);
        if (ilan == null) {
            return ResponseEntity.badRequest().body("İlan bulunamadı.");
        }

        String sebep = veri.get("sebep");
        if (sebep == null || sebep.trim().isEmpty()) {
            sebep = "Yönetim kararı.";
        }

        try {
            // İşverene bildirim
            Bildirim isverenBildirim = new Bildirim();
            isverenBildirim.setKullanici(ilan.getIsveren().getKullanici());
            isverenBildirim.setMesaj("⚠️ '" + ilan.getBaslik() + "' başlıklı ilanınız yayından kaldırıldı. \nSebep: " + sebep);
            bildirimService.kaydet(isverenBildirim);

            // Başvuranlara bildirim ve başvuruların silinmesi
            List<Basvuru> basvurular = ilan.getBasvurular();
            if (basvurular != null && !basvurular.isEmpty()) {
                for (Basvuru basvuru : basvurular) {
                    Bildirim adayBildirim = new Bildirim();
                    adayBildirim.setKullanici(basvuru.getIsArayan().getKullanici());
                    adayBildirim.setMesaj("❌ Başvurduğunuz '" + ilan.getBaslik() + "' ilanı yayından kaldırıldığı için başvurunuz iptal edildi. \nSebep: " + sebep);
                    bildirimService.kaydet(adayBildirim);
                }
                basvuruRepository.deleteAll(basvurular); 
            }

            favoriRepository.deleteByIsIlaniId(id);
            isIlaniRepository.deleteById(id);

            return ResponseEntity.ok("İlan kaldırıldı, işverene ve adaylara bildirim gönderildi.");
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Hata oluştu: " + e.getMessage());
        }
    }

    // --- 4. PAKET YÖNETİMİ ---
    @GetMapping("/paketler")
    public ResponseEntity<List<Paket>> paketleriGetir() {
        return ResponseEntity.ok(paketRepository.findAll());
    }

    @PostMapping("/paket-kaydet")
    public ResponseEntity<?> paketKaydet(@RequestBody Paket paket) {
        try {
            paketRepository.save(paket);
            return ResponseEntity.ok("Paket başarıyla kaydedildi.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Hata: " + e.getMessage());
        }
    }

    @DeleteMapping("/paket-sil/{id}")
    public ResponseEntity<?> paketSil(@PathVariable Long id) {
        if (!paketRepository.existsById(id)) return ResponseEntity.badRequest().body("Paket bulunamadı.");
        paketRepository.deleteById(id);
        return ResponseEntity.ok("Paket silindi.");
    }
}