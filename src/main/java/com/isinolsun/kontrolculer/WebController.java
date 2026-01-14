package com.isinolsun.kontrolculer;

import com.isinolsun.depolar.*;
import com.isinolsun.servisler.*;
import com.isinolsun.tipler.BasvuruDurumu;
import com.isinolsun.tipler.KullaniciRolu;
import com.isinolsun.varliklar.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

@Controller
public class WebController {

    private final IsIlaniService isIlaniService;
    private final KullaniciService kullaniciService;
    private final IsverenRepository isverenRepository;
    private final IsArayanRepository isArayanRepository;
    private final BasvuruService basvuruService;
    private final BasvuruRepository basvuruRepository;
    private final KullaniciRepository kullaniciRepository;
    private final BildirimService bildirimService;
    private final FavoriService favoriService;
    private final PaketRepository paketRepository;
    private final IyzicoService iyzicoService;
    private final GeminiService geminiService;
    private final FavoriRepository favoriRepository;

    public WebController(IsIlaniService isIlaniService, 
                         KullaniciService kullaniciService, 
                         IsverenRepository isverenRepository,
                         IsArayanRepository isArayanRepository,
                         BasvuruService basvuruService,
                         BasvuruRepository basvuruRepository,
                         KullaniciRepository kullaniciRepository,
                         BildirimService bildirimService,
                         FavoriService favoriService,
                         PaketRepository paketRepository,
                         IyzicoService iyzicoService,
                         GeminiService geminiService,
                         FavoriRepository favoriRepository) {
        this.isIlaniService = isIlaniService;
        this.kullaniciService = kullaniciService;
        this.isverenRepository = isverenRepository;
        this.isArayanRepository = isArayanRepository;
        this.basvuruService = basvuruService;
        this.basvuruRepository = basvuruRepository;
        this.kullaniciRepository = kullaniciRepository;
        this.bildirimService = bildirimService;
        this.favoriService = favoriService;
        this.paketRepository = paketRepository;
        this.iyzicoService = iyzicoService;
        this.geminiService = geminiService;
        this.favoriRepository = favoriRepository;
    }

    @PostConstruct
    public void otomatikAdminOlustur() {
        if (!kullaniciRepository.existsByRol(KullaniciRolu.ADMIN)) {
            Kullanici admin = new Kullanici();
            admin.setEmail("admin@isinolsun.com");
            admin.setSifre("admin123");
            admin.setRol(KullaniciRolu.ADMIN);
            admin.setTelefon("0000000000");
            kullaniciRepository.save(admin);
        }
    }

    @GetMapping("/")
    public String anasayfa(Model model, HttpSession session, 
                           @RequestParam(required = false) String kelime, 
                           @RequestParam(required = false) String sehir) {
        
        List<IsIlani> ilanlar = isIlaniService.gelismisAra(kelime, sehir);
        model.addAttribute("ilanlar", ilanlar);

        Map<Long, Integer> basvuruSayilari = new HashMap<>();
        for (IsIlani ilan : ilanlar) {
            int sayi = basvuruService.ilanaGelenBasvurular(ilan.getId()).size();
            basvuruSayilari.put(ilan.getId(), sayi);
        }
        model.addAttribute("basvuruSayilari", basvuruSayilari);

        Kullanici girisYapan = (Kullanici) session.getAttribute("girisYapanKullanici");
        List<Long> favoriIlanIdleri = new ArrayList<>();

        if (girisYapan != null) {
            List<Bildirim> bildirimler = bildirimService.kullaniciBildirimleri(girisYapan.getId());
            model.addAttribute("bildirimler", bildirimler);

            if (girisYapan.getRol() == KullaniciRolu.IS_ARAYAN) {
                List<Long> basvurulanIlanlar = basvuruService.kullanicininBasvurduguIlanIdleri(girisYapan.getId());
                model.addAttribute("basvurulanIlanlar", basvurulanIlanlar);
                
                Optional<IsArayan> isArayanOpt = isArayanRepository.findByKullaniciId(girisYapan.getId());
                if(isArayanOpt.isPresent()) {
                    List<Favori> favlar = favoriService.getKullaniciFavorileri(isArayanOpt.get().getId());
                    favoriIlanIdleri = favlar.stream().map(f -> f.getIsIlani().getId()).toList();
                }
            }
        }
        model.addAttribute("favoriIlanIdleri", favoriIlanIdleri);
        return "index";
    }

    @GetMapping("/favori-islem/{ilanId}")
    public String favoriIslem(@PathVariable Long ilanId, HttpSession session, @RequestParam(required = false) String redirect) {
        Kullanici k = (Kullanici) session.getAttribute("girisYapanKullanici");
        if (k != null && k.getRol() == KullaniciRolu.IS_ARAYAN) {
            IsArayan isArayan = isArayanRepository.findByKullaniciId(k.getId()).orElse(null);
            IsIlani ilan = isIlaniService.ilanGetir(ilanId);
            if (isArayan != null && ilan != null) {
                favoriService.favoriIslemi(isArayan, ilan);
            }
        }
        if ("detay".equals(redirect)) return "redirect:/ilan/" + ilanId;
        if ("favoriler".equals(redirect)) return "redirect:/favorilerim";
        return "redirect:/";
    }

    @GetMapping("/favorilerim")
    public String favorilerim(HttpSession session, Model model) {
        Kullanici k = (Kullanici) session.getAttribute("girisYapanKullanici");
        if (k == null || k.getRol() != KullaniciRolu.IS_ARAYAN) return "redirect:/";
        IsArayan isArayan = isArayanRepository.findByKullaniciId(k.getId()).orElse(null);
        if (isArayan != null) {
            model.addAttribute("favoriler", favoriService.getKullaniciFavorileri(isArayan.getId()));
        }
        return "favorilerim";
    }

    @GetMapping("/admin")
    public String adminPanel(HttpSession session, Model model) {
        Kullanici k = (Kullanici) session.getAttribute("girisYapanKullanici");
        if (k == null || k.getRol() != KullaniciRolu.ADMIN) return "redirect:/";
        
        List<Kullanici> kullanicilar = kullaniciRepository.findAll();
        List<IsIlani> ilanlar = isIlaniService.tumAktifIlanlariGetir();
        
        model.addAttribute("tumKullanicilar", kullanicilar);
        model.addAttribute("tumIlanlar", ilanlar);
        model.addAttribute("paketler", paketRepository.findAll()); 
        
        long isverenSayisi = kullanicilar.stream().filter(u -> u.getRol() == KullaniciRolu.ISVEREN).count();
        long isArayanSayisi = kullanicilar.stream().filter(u -> u.getRol() == KullaniciRolu.IS_ARAYAN).count();
        model.addAttribute("isverenSayisi", isverenSayisi);
        model.addAttribute("isArayanSayisi", isArayanSayisi);
        
        Map<String, Long> sehirDagilimi = ilanlar.stream()
            .collect(Collectors.groupingBy(IsIlani::getSehir, Collectors.counting()));
        
        model.addAttribute("sehirIsimleri", new ArrayList<>(sehirDagilimi.keySet()));
        model.addAttribute("sehirSayilari", new ArrayList<>(sehirDagilimi.values()));
        
        return "admin-panel";
    }

    @PostMapping("/admin/paket-olustur")
    public String paketOlustur(@RequestParam String ad, @RequestParam int gun, @RequestParam double fiyat, HttpSession session) {
        Kullanici k = (Kullanici) session.getAttribute("girisYapanKullanici");
        if (k == null || k.getRol() != KullaniciRolu.ADMIN) return "redirect:/";
        Paket p = new Paket();
        p.setAd(ad); p.setGun(gun); p.setFiyat(fiyat);
        paketRepository.save(p);
        return "redirect:/admin";
    }

    @GetMapping("/ilan/{id}/one-cikar")
    public String oneCikarSayfasi(@PathVariable Long id, Model model, HttpSession session) {
        Kullanici k = (Kullanici) session.getAttribute("girisYapanKullanici");
        if (k == null || k.getRol() != KullaniciRolu.ISVEREN) return "redirect:/";
        
        IsIlani ilan = isIlaniService.ilanGetir(id);
        if (!ilan.getIsveren().getKullanici().getId().equals(k.getId())) return "redirect:/ilanlarim";
        
        model.addAttribute("ilan", ilan);
        model.addAttribute("paketler", paketRepository.findAll());
        return "paket-sec";
    }

    @PostMapping("/ilan/{id}/paket-satin-al")
    public String paketSatinAl(@PathVariable Long id, 
                               @RequestParam Long paketId, 
                               @RequestParam String kartSahibi,
                               @RequestParam String kartNo,
                               @RequestParam String ay,
                               @RequestParam String yil,
                               @RequestParam String cvv,
                               HttpSession session) {
                               
        Kullanici k = (Kullanici) session.getAttribute("girisYapanKullanici");
        if (k == null || k.getRol() != KullaniciRolu.ISVEREN) return "redirect:/";
        
        IsIlani ilan = isIlaniService.ilanGetir(id);
        Paket paket = paketRepository.findById(paketId).orElse(null);
        
        if (ilan != null && paket != null) {
            boolean odemeBasarili = iyzicoService.odemeYap(k, paket.getFiyat(), kartSahibi, kartNo, ay, yil, cvv);
            
            if (odemeBasarili) {
                ilan.setVitrinBitisTarihi(java.time.LocalDateTime.now().plusDays(paket.getGun()));
                isIlaniService.ilanKaydet(ilan);
                return "redirect:/ilanlarim?msg=vitrin_basarili";
            } else {
                return "redirect:/ilan/" + id + "/one-cikar?hata=odeme_basarisiz";
            }
        }
        return "redirect:/ilanlarim";
    }

    @GetMapping("/ilan/{id}")
    public String ilanDetay(@PathVariable Long id, @RequestParam(required = false) String hata, @RequestParam(required = false) Boolean basarili, HttpSession session, Model model) {
        IsIlani ilan = isIlaniService.ilanGetir(id);
        if (ilan == null) return "redirect:/";
        model.addAttribute("ilan", ilan);
        
        int basvuruSayisi = basvuruService.ilanaGelenBasvurular(id).size();
        model.addAttribute("basvuruSayisi", basvuruSayisi);
        model.addAttribute("hataKodu", hata);
        model.addAttribute("basariDurumu", basarili);
        
        Kullanici k = (Kullanici) session.getAttribute("girisYapanKullanici");
        boolean basvuruYapildi = false;
        boolean favoriMi = false;
        boolean cvVarMi = false; 

        if (k != null && k.getRol() == KullaniciRolu.IS_ARAYAN) {
            IsArayan isArayan = isArayanRepository.findByKullaniciId(k.getId()).orElse(null);
            if (isArayan != null) {
                if (isArayan.getCvDosya() != null && isArayan.getCvDosya().length > 0) {
                    cvVarMi = true;
                }
                
                try {
                    List<Long> basvurulanIdler = basvuruService.kullanicininBasvurduguIlanIdleri(k.getId());
                    if (basvurulanIdler != null && basvurulanIdler.contains(id)) basvuruYapildi = true;
                    favoriMi = favoriService.favoriMi(isArayan.getId(), id);
                } catch (Exception e) {}
            }
        }
        
        model.addAttribute("basvuruYapildi", basvuruYapildi);
        model.addAttribute("favoriMi", favoriMi);
        model.addAttribute("cvVarMi", cvVarMi); 
        
        return "ilan-detay";
    }

    @PostMapping("/ilan/{id}/basvur")
    public String basvuruYap(@PathVariable Long id, HttpSession session) {
        Kullanici k = (Kullanici) session.getAttribute("girisYapanKullanici");
        if (k == null) return "redirect:/giris";
        if (k.getRol() == KullaniciRolu.ISVEREN) return "redirect:/ilan/" + id + "?hata=isveren";
        IsArayan isArayan = isArayanRepository.findByKullaniciId(k.getId()).orElse(null);
        IsIlani ilan = isIlaniService.ilanGetir(id);
        if (isArayan != null && ilan != null) {
            try {
                Basvuru yeniBasvuru = new Basvuru(); yeniBasvuru.setIsArayan(isArayan); yeniBasvuru.setIsIlani(ilan);
                basvuruService.basvuruYap(yeniBasvuru);
            } catch (RuntimeException e) { return "redirect:/ilan/" + id + "?hata=zatenvar"; }
        }
        return "redirect:/ilan/" + id + "?basarili";
    }

    @GetMapping("/bildirim-sil/{id}") public String bildirimSil(@PathVariable Long id, HttpSession session) { if (session.getAttribute("girisYapanKullanici") != null) bildirimService.sil(id); return "redirect:/"; }
    @GetMapping("/kayit") public String kayitSayfasi() { return "kayit"; }
    @GetMapping("/giris") public String girisSayfasi() { return "giris"; }
    @GetMapping("/cikis") public String cikisYap(HttpSession session) { session.invalidate(); return "redirect:/"; }
    
    // --- GÜNCELLENMİŞ KAYIT METODU ---
    @PostMapping("/kayit") 
    public String kayitOl(@ModelAttribute Kullanici kullanici, @RequestParam String isim) { 
        
        // 1. ÖNCE E-POSTA VAR MI KONTROL ET
    	Kullanici mevcut = kullaniciRepository.findByEmail(kullanici.getEmail()).orElse(null);
        if (mevcut != null) {
            // E-posta varsa hata parametresiyle kayıt sayfasına dön
            return "redirect:/kayit?hata=eposta_kullanimda";
        }

        // 2. YOKSA KAYDET
        Kullanici kaydedilenUser = kullaniciService.kullaniciKaydet(kullanici); 
        
        if (kaydedilenUser.getRol() == KullaniciRolu.ISVEREN) { 
            Isveren isveren = new Isveren(); isveren.setKullanici(kaydedilenUser); isveren.setSirketAdi(isim); isverenRepository.save(isveren); 
        } else { 
            IsArayan isArayan = new IsArayan(); isArayan.setKullanici(kaydedilenUser); isArayan.setAd(isim); isArayan.setSoyad(""); isArayanRepository.save(isArayan); 
        } 
        return "redirect:/giris"; 
    }
    // ------------------------------------
    
    @PostMapping("/giris") public String girisYap(@RequestParam String email, @RequestParam String sifre, HttpSession session) { 
        Kullanici kullanici = kullaniciService.girisYap(email, sifre); 
        if (kullanici != null) { 
            session.setAttribute("girisYapanKullanici", kullanici); 
            if (kullanici.getRol() == KullaniciRolu.ADMIN) return "redirect:/admin"; 
            return "redirect:/"; 
        } 
        return "redirect:/giris?error"; 
    }
    
    @GetMapping("/admin/kullanici-sil/{id}")
    public String kullaniciSil(@PathVariable Long id, HttpSession session) {
        Kullanici admin = (Kullanici) session.getAttribute("girisYapanKullanici");
        if (admin == null || admin.getRol() != KullaniciRolu.ADMIN) return "redirect:/admin";
        if (admin.getId().equals(id)) return "redirect:/admin?hata=kendini_silemezsin";

        Kullanici silinecekKullanici = kullaniciRepository.findById(id).orElse(null);

        if (silinecekKullanici != null) {
            try {
                if (silinecekKullanici.getRol() == KullaniciRolu.IS_ARAYAN) {
                    Optional<IsArayan> isArayanOpt = isArayanRepository.findByKullaniciId(id);
                    if (isArayanOpt.isPresent()) {
                        IsArayan isArayan = isArayanOpt.get();
                        
                        List<Favori> favoriler = favoriService.getKullaniciFavorileri(isArayan.getId());
                        if (favoriler != null && !favoriler.isEmpty()) {
                            favoriRepository.deleteAll(favoriler); 
                        }
                        
                        List<Basvuru> basvurular = basvuruService.kullanicininBasvurulariniGetir(silinecekKullanici.getId());
                        if (basvurular != null && !basvurular.isEmpty()) {
                            basvuruRepository.deleteAll(basvurular);
                        }

                        isArayanRepository.delete(isArayan);
                    }
                } 
                else if (silinecekKullanici.getRol() == KullaniciRolu.ISVEREN) {
                    Optional<Isveren> isverenOpt = isverenRepository.findByKullaniciId(id);
                    if (isverenOpt.isPresent()) {
                        Isveren isveren = isverenOpt.get();
                        
                        List<IsIlani> ilanlar = isIlaniService.isvereninIlanlariniGetir(isveren.getId());
                        for (IsIlani ilan : ilanlar) {
                            isIlaniService.ilanSil(ilan.getId());
                        }
                        
                        isverenRepository.delete(isveren);
                    }
                }

                kullaniciRepository.deleteById(id);
                
            } catch (Exception e) {
                e.printStackTrace();
                return "redirect:/admin?hata=silme_basarisiz_detayli";
            }
        }

        return "redirect:/admin?msg=kullanici_silindi";
    }
    
    @GetMapping("/admin/ilan-sil/{id}") 
    public String adminIlanSil(@PathVariable Long id, @RequestParam(required = false) String sebep, HttpSession session) { 
        Kullanici k = (Kullanici) session.getAttribute("girisYapanKullanici"); 
        if (k == null || k.getRol() != KullaniciRolu.ADMIN) return "redirect:/admin"; 
        
        IsIlani ilan = isIlaniService.ilanGetir(id); 
        if (ilan != null) { 
            
            Bildirim bildirim = new Bildirim(); 
            bildirim.setKullanici(ilan.getIsveren().getKullanici()); 
            bildirim.setMesaj("'" + ilan.getBaslik() + "' başlıklı ilanınız şu sebeple kaldırılmıştır: " + sebep); 
            bildirimService.kaydet(bildirim); 
            
            List<Basvuru> basvurular = basvuruService.ilanaGelenBasvurular(id);
            if (basvurular != null && !basvurular.isEmpty()) {
                for (Basvuru basvuru : basvurular) {
                    Kullanici adayKullanici = basvuru.getIsArayan().getKullanici();
                    if (adayKullanici != null) {
                        Bildirim adayBildirimi = new Bildirim();
                        adayBildirimi.setKullanici(adayKullanici);
                        adayBildirimi.setMesaj("Başvurduğunuz '" + ilan.getBaslik() + "' ilanı yayından kaldırılmıştır.");
                        bildirimService.kaydet(adayBildirimi);
                    }
                }
            }
            
            isIlaniService.ilanSil(id); 
        } 
        return "redirect:/admin?msg=silindi"; 
    }

    @GetMapping("/ilan-ekle") public String ilanEkleSayfasi(HttpSession session) { Kullanici k = (Kullanici) session.getAttribute("girisYapanKullanici"); if (k == null || k.getRol() != KullaniciRolu.ISVEREN) return "redirect:/"; return "ilan-ekle"; }
    
    @PostMapping("/ilan-ekle") public String ilanKaydet(@ModelAttribute IsIlani isIlani, HttpSession session) { 
        Kullanici k = (Kullanici) session.getAttribute("girisYapanKullanici"); 
        if (k != null && k.getRol() == KullaniciRolu.ISVEREN) { 
            Isveren isveren = isverenRepository.findByKullaniciId(k.getId()).orElse(null); 
            if (isveren != null) { isIlani.setIsveren(isveren); isIlaniService.ilanKaydet(isIlani); } 
        } 
        return "redirect:/"; 
    }
    
    @GetMapping("/profil") 
    public String profilSayfasi(HttpSession session, Model model) { 
        Kullanici k = (Kullanici) session.getAttribute("girisYapanKullanici"); 
        if (k == null) return "redirect:/giris"; 
        if (k.getRol() == KullaniciRolu.ADMIN) return "redirect:/admin"; 
        
        if (k.getRol() == KullaniciRolu.ISVEREN) { 
            Isveren isveren = isverenRepository.findByKullaniciId(k.getId()).orElse(null); 
            model.addAttribute("profil", isveren); 
            model.addAttribute("rol", "ISVEREN"); 
        } else { 
            IsArayan isArayan = isArayanRepository.findByKullaniciId(k.getId()).orElse(null); 
            model.addAttribute("profil", isArayan); 
            model.addAttribute("rol", "IS_ARAYAN"); 
            
            if (isArayan != null && isArayan.getProfilResmi() != null && isArayan.getProfilResmi().length > 0) {
                String base64Resim = java.util.Base64.getEncoder().encodeToString(isArayan.getProfilResmi());
                model.addAttribute("profilResmiBase64", base64Resim);
            }
        } 
        return "profil"; 
    }
    
    @PostMapping("/profil/guncelle") 
    public String profilGuncelle(HttpSession session, 
                                 @RequestParam(required = false) String sirketAdi, 
                                 @RequestParam(required = false) String webSitesi, 
                                 @RequestParam(required = false) String hakkimizda, 
                                 @RequestParam(required = false) String adres, 
                                 @RequestParam(required = false) MultipartFile logoDosya, 
                                 @RequestParam(required = false) Double enlem, 
                                 @RequestParam(required = false) Double boylam, 
                                 @RequestParam(required = false) String ad, 
                                 @RequestParam(required = false) String meslek, 
                                 @RequestParam(required = false) String ozetBilgi, 
                                 @RequestParam(required = false) MultipartFile cvDosya, 
                                 @RequestParam(required = false) MultipartFile profilResmiDosya, 
                                 @RequestParam String telefon) throws IOException { 
        
        Kullanici k = (Kullanici) session.getAttribute("girisYapanKullanici"); 
        if (k == null) return "redirect:/giris"; 
        if (k.getRol() == KullaniciRolu.ADMIN) return "redirect:/admin"; 
        
        k.setTelefon(telefon); 
        kullaniciService.kullaniciGuncelle(k); 
        session.setAttribute("girisYapanKullanici", k); 
        
        if (k.getRol() == KullaniciRolu.ISVEREN) { 
            Isveren isveren = isverenRepository.findByKullaniciId(k.getId()).orElse(null); 
            if(isveren != null) {
                isveren.setSirketAdi(sirketAdi); isveren.setWebSitesi(webSitesi); isveren.setHakkimizda(hakkimizda); isveren.setAdres(adres); isveren.setEnlem(enlem); isveren.setBoylam(boylam); 
                if (logoDosya != null && !logoDosya.isEmpty()) isveren.setLogo(logoDosya.getBytes()); 
                isverenRepository.save(isveren); 
            }
        } else { 
            IsArayan isArayan = isArayanRepository.findByKullaniciId(k.getId()).orElse(null); 
            if(isArayan != null) {
                isArayan.setAd(ad); isArayan.setMeslek(meslek); isArayan.setOzet_bilgi(ozetBilgi); 
                if (cvDosya != null && !cvDosya.isEmpty()) { 
                    isArayan.setCvDosya(cvDosya.getBytes()); 
                    isArayan.setCvDosyaAdi(cvDosya.getOriginalFilename()); 
                } 
                if (profilResmiDosya != null && !profilResmiDosya.isEmpty()) {
                    isArayan.setProfilResmi(profilResmiDosya.getBytes());
                }
                isArayanRepository.save(isArayan); 
            }
        } 
        return "redirect:/profil?basarili"; 
    }
    
    @GetMapping("/basvurularim") public String basvurularim(HttpSession session, Model model) { Kullanici k = (Kullanici) session.getAttribute("girisYapanKullanici"); if (k == null || k.getRol() != KullaniciRolu.IS_ARAYAN) return "redirect:/"; model.addAttribute("basvurular", basvuruService.kullanicininBasvurulariniGetir(k.getId())); return "basvurularim"; }
    
    @GetMapping("/ilanlarim") public String ilanlarim(HttpSession session, Model model) { 
        Kullanici k = (Kullanici) session.getAttribute("girisYapanKullanici"); 
        if (k == null || k.getRol() != KullaniciRolu.ISVEREN) return "redirect:/"; 
        Isveren isveren = isverenRepository.findByKullaniciId(k.getId()).orElse(null); 
        if(isveren != null) {
            model.addAttribute("ilanlar", isIlaniService.isvereninIlanlariniGetir(isveren.getId())); 
        }
        return "ilanlarim"; 
    }
    
    @GetMapping("/ilan/{id}/basvurular") 
    public String basvurulariGor(@PathVariable Long id, HttpSession session, Model model) { 
        Kullanici k = (Kullanici) session.getAttribute("girisYapanKullanici"); 
        if (k == null || k.getRol() != KullaniciRolu.ISVEREN) return "redirect:/ilanlarim"; 
        
        IsIlani ilan = isIlaniService.ilanGetir(id); 
        Isveren isveren = isverenRepository.findByKullaniciId(k.getId()).orElse(null); 
        
        if (isveren == null || !ilan.getIsveren().getId().equals(isveren.getId())) return "redirect:/ilanlarim"; 
        
        List<Basvuru> basvurular = basvuruService.ilanaGelenBasvurular(id);
        
        Map<Long, String> adayResimleri = new HashMap<>();
        for (Basvuru b : basvurular) {
            if (b.getIsArayan() != null && b.getIsArayan().getProfilResmi() != null && b.getIsArayan().getProfilResmi().length > 0) {
                String base64 = java.util.Base64.getEncoder().encodeToString(b.getIsArayan().getProfilResmi());
                adayResimleri.put(b.getId(), base64);
            }
        }
        model.addAttribute("adayResimleri", adayResimleri);

        model.addAttribute("basvurular", basvurular); 
        model.addAttribute("ilan", ilan); 
        return "basvuru-listesi"; 
    }
    
    @GetMapping("/basvuru/{basvuruId}/cv-indir") public ResponseEntity<ByteArrayResource> cvIndir(@PathVariable Long basvuruId) { Basvuru basvuru = basvuruRepository.findById(basvuruId).orElse(null); if (basvuru == null || basvuru.getIsArayan().getCvDosya() == null) { return ResponseEntity.notFound().build(); } IsArayan aday = basvuru.getIsArayan(); ByteArrayResource resource = new ByteArrayResource(aday.getCvDosya()); return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + aday.getCvDosyaAdi() + "\"").contentType(MediaType.APPLICATION_PDF).body(resource); }
    
    @GetMapping("/ilan-sil/{id}") public String ilanSil(@PathVariable Long id, HttpSession session) { 
        Kullanici k = (Kullanici) session.getAttribute("girisYapanKullanici"); 
        if (k == null || k.getRol() != KullaniciRolu.ISVEREN) return "redirect:/"; 
        IsIlani ilan = isIlaniService.ilanGetir(id); 
        Isveren isveren = isverenRepository.findByKullaniciId(k.getId()).orElse(null); 
        if (isveren != null && ilan != null && ilan.getIsveren().getId().equals(isveren.getId())) { isIlaniService.ilanSil(id); } 
        return "redirect:/ilanlarim"; 
    }
    
    @GetMapping("/ilan-duzenle/{id}") public String ilanDuzenleSayfasi(@PathVariable Long id, HttpSession session, Model model) { 
        Kullanici k = (Kullanici) session.getAttribute("girisYapanKullanici"); 
        if (k == null || k.getRol() != KullaniciRolu.ISVEREN) return "redirect:/"; 
        IsIlani ilan = isIlaniService.ilanGetir(id); 
        Isveren isveren = isverenRepository.findByKullaniciId(k.getId()).orElse(null); 
        if (isveren == null || ilan == null || !ilan.getIsveren().getId().equals(isveren.getId())) return "redirect:/ilanlarim"; 
        model.addAttribute("ilan", ilan); return "ilan-duzenle"; 
    }
    
    @PostMapping("/ilan-duzenle") public String ilanGuncelle(@ModelAttribute IsIlani isIlani, HttpSession session) { 
        Kullanici k = (Kullanici) session.getAttribute("girisYapanKullanici"); 
        IsIlani mevcutIlan = isIlaniService.ilanGetir(isIlani.getId()); 
        Isveren isveren = isverenRepository.findByKullaniciId(k.getId()).orElse(null); 
        if (isveren != null && mevcutIlan != null && mevcutIlan.getIsveren().getId().equals(isveren.getId())) { 
            mevcutIlan.setBaslik(isIlani.getBaslik()); mevcutIlan.setSehir(isIlani.getSehir()); mevcutIlan.setMaas(isIlani.getMaas()); mevcutIlan.setAciklama(isIlani.getAciklama()); 
            isIlaniService.ilanKaydet(mevcutIlan); 
        } 
        return "redirect:/ilanlarim"; 
    }
    
    @PostMapping("/basvuru-guncelle") public String basvuruGuncelle(@RequestParam Long basvuruId, @RequestParam String durum, @RequestParam(required = false) String mesaj, @RequestParam(required = false) String tarih, @RequestParam(required = false) boolean ilaniKapat, HttpSession session) { Kullanici k = (Kullanici) session.getAttribute("girisYapanKullanici"); if (k == null || k.getRol() != KullaniciRolu.ISVEREN) return "redirect:/"; Basvuru basvuru = basvuruService.basvuruGetir(basvuruId); if (basvuru != null && basvuru.getIsIlani().getIsveren().getKullanici().getId().equals(k.getId())) { BasvuruDurumu yeniDurum = BasvuruDurumu.valueOf(durum); java.time.LocalDateTime mulakatTarihi = null; if (tarih != null && !tarih.isEmpty()) { mulakatTarihi = java.time.LocalDateTime.parse(tarih); } basvuruService.durumGuncelle(basvuruId, yeniDurum, mesaj, mulakatTarihi); if (yeniDurum == BasvuruDurumu.KABUL_EDILDI && ilaniKapat) { IsIlani ilan = basvuru.getIsIlani(); ilan.setAktif(false); isIlaniService.ilanKaydet(ilan); } } return "redirect:/ilan/" + basvuru.getIsIlani().getId() + "/basvurular"; }
    @GetMapping("/ilan-tekrar-yayinla/{id}") public String ilanTekrarYayinla(@PathVariable Long id, HttpSession session) { Kullanici k = (Kullanici) session.getAttribute("girisYapanKullanici"); if (k == null || k.getRol() != KullaniciRolu.ISVEREN) return "redirect:/"; IsIlani ilan = isIlaniService.ilanGetir(id); if (ilan != null && ilan.getIsveren().getKullanici().getId().equals(k.getId())) { ilan.setAktif(true); isIlaniService.ilanKaydet(ilan); } return "redirect:/ilan/" + id + "/basvurular"; }
    
    @GetMapping("/admin/paket-sil/{id}")
    public String paketSil(@PathVariable Long id, HttpSession session) {
        Kullanici k = (Kullanici) session.getAttribute("girisYapanKullanici");
        if (k == null || k.getRol() != KullaniciRolu.ADMIN) return "redirect:/";
        paketRepository.deleteById(id);
        return "redirect:/admin?msg=paket_silindi";
    }

    @GetMapping("/admin/paket-duzenle/{id}")
    public String paketDuzenleSayfasi(@PathVariable Long id, Model model, HttpSession session) {
        Kullanici k = (Kullanici) session.getAttribute("girisYapanKullanici");
        if (k == null || k.getRol() != KullaniciRolu.ADMIN) return "redirect:/";
        Paket paket = paketRepository.findById(id).orElse(null);
        if (paket == null) return "redirect:/admin";
        model.addAttribute("paket", paket);
        return "admin-paket-duzenle"; 
    }

    @PostMapping("/admin/paket-guncelle")
    public String paketGuncelle(@ModelAttribute Paket paket, HttpSession session) {
        Kullanici k = (Kullanici) session.getAttribute("girisYapanKullanici");
        if (k == null || k.getRol() != KullaniciRolu.ADMIN) return "redirect:/";
        paketRepository.save(paket);
        return "redirect:/admin?msg=paket_guncellendi";
    }
    
    @GetMapping("/api/ai/cv-tavsiye")
    @ResponseBody
    public String cvTavsiyesiAl(HttpSession session) {
        Kullanici k = (Kullanici) session.getAttribute("girisYapanKullanici");
        if (k == null || k.getRol() != KullaniciRolu.IS_ARAYAN) return "Lütfen giriş yapın.";
        IsArayan isArayan = isArayanRepository.findByKullaniciId(k.getId()).orElse(null);
        if (isArayan == null) return "Profil bilgisi bulunamadı.";
        return geminiService.kariyerAnaliziOlustur(isArayan.getMeslek(), isArayan.getOzet_bilgi(), "");
    }
    
    @PostMapping("/api/ai/sohbet")
    @ResponseBody
    public String genelSohbet(@RequestParam String mesaj) {
        String prompt = "Sen 'İşin Olsun' platformunun yardımsever kariyer asistanısın. Kullanıcı sana şunu sordu: '" + mesaj + "'. Kısa, samimi ve yardımcı olacak şekilde cevap ver. Cevabın 2-3 cümleyi geçmesin.";
        return geminiService.yapayZekayaSor(prompt);
    }
    
    @GetMapping("/api/ai/ilan-olustur")
    @ResponseBody
    public String ilanMetniOlustur(@RequestParam String baslik, HttpSession session) {
        Kullanici k = (Kullanici) session.getAttribute("girisYapanKullanici");
        if (k == null || k.getRol() != KullaniciRolu.ISVEREN) return "Yetkisiz işlem.";
        Isveren isveren = isverenRepository.findByKullaniciId(k.getId()).orElse(null);
        String sirketAdi = (isveren != null) ? isveren.getSirketAdi() : "Şirketimiz";
        return geminiService.ilanMetniOlustur(baslik, sirketAdi);
    }
}