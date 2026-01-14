package com.isinolsun.mobil;

import com.isinolsun.depolar.BildirimRepository;
import com.isinolsun.depolar.IsArayanRepository;
import com.isinolsun.depolar.IsverenRepository;
import com.isinolsun.depolar.KullaniciRepository;
import com.isinolsun.dto.MobilGirisDTO;
import com.isinolsun.servisler.KullaniciService;
import com.isinolsun.tipler.KullaniciRolu;
import com.isinolsun.varliklar.IsArayan;
import com.isinolsun.varliklar.Isveren;
import com.isinolsun.varliklar.Kullanici;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/mobil/hesap")
@CrossOrigin(origins = "*") // React Native erişimi için
public class MobilHesapController {

    private final KullaniciService kullaniciService;
    
    @Autowired private IsArayanRepository isArayanRepository;
    @Autowired private IsverenRepository isverenRepository;
    @Autowired private KullaniciRepository kullaniciRepository;
    @Autowired private BildirimRepository bildirimRepository;

    public MobilHesapController(KullaniciService kullaniciService) {
        this.kullaniciService = kullaniciService;
    }

    // --- 1. GİRİŞ YAP ---
    @PostMapping("/giris")
    public ResponseEntity<?> mobildenGirisYap(@RequestBody Map<String, String> girisBilgileri) {
        String email = girisBilgileri.get("email");
        String sifre = girisBilgileri.get("sifre");

        Kullanici kullanici = kullaniciService.girisYap(email, sifre);

        if (kullanici == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Hatalı E-posta veya Şifre");
        }

        String gonderilecekAd = "Kullanıcı";
        String gonderilecekTelefon = kullanici.getTelefon();
        String logoBase64 = null;
        
        if (kullanici.getRol() == KullaniciRolu.IS_ARAYAN) {
            Optional<IsArayan> isArayanOpt = isArayanRepository.findByKullaniciId(kullanici.getId());
            if (isArayanOpt.isPresent()) {
                IsArayan isArayan = isArayanOpt.get();
                gonderilecekAd = isArayan.getAd() + " " + (isArayan.getSoyad() != null ? isArayan.getSoyad() : "");
                
                if (isArayan.getProfilResmi() != null) {
                    try {
                        byte[] resized = resizeImage(isArayan.getProfilResmi(), 150, 150);
                        if (resized != null) {
                            logoBase64 = Base64.getEncoder().encodeToString(resized);
                        }
                    } catch (Exception e) { System.err.println("Resim hatası: " + e.getMessage()); }
                }
            }
        } 
        else if (kullanici.getRol() == KullaniciRolu.ISVEREN) {
            Optional<Isveren> isverenOpt = isverenRepository.findByKullaniciId(kullanici.getId());
            if (isverenOpt.isPresent()) {
                Isveren isveren = isverenOpt.get();
                gonderilecekAd = isveren.getSirketAdi();
                
                if (isveren.getLogo() != null) {
                    try {
                        byte[] resizedLogoBytes = resizeImage(isveren.getLogo(), 150, 150);
                        if (resizedLogoBytes != null) {
                            logoBase64 = Base64.getEncoder().encodeToString(resizedLogoBytes);
                        }
                    } catch (Exception e) { System.err.println("Logo hatası: " + e.getMessage()); }
                }
            }
        }

        MobilGirisDTO sonuc = new MobilGirisDTO(
            kullanici.getId(),
            kullanici.getEmail(),
            gonderilecekAd,
            gonderilecekTelefon,
            kullanici.getRol(),
            logoBase64
        );

        return ResponseEntity.ok(sonuc);
    }

    // --- 2. KAYIT OL (DÜZELTİLDİ: setIsim kaldırıldı) ---
    @PostMapping("/kayit")
    public ResponseEntity<?> mobildenKayitOl(@RequestBody Map<String, String> veri) {
        try {
            String email = veri.get("email");
            String sifre = veri.get("sifre");
            String telefon = veri.get("telefon");
            String rolStr = veri.get("rol");
            String ad = veri.get("ad"); // Frontend'den gelen isim veya şirket adı

            if (email == null || sifre == null || rolStr == null) {
                return ResponseEntity.badRequest().body("Eksik bilgi.");
            }

            KullaniciRolu rol = KullaniciRolu.valueOf(rolStr);

            // 1. Ana Kullanıcıyı Oluştur
            Kullanici yeniKullanici = new Kullanici();
            yeniKullanici.setEmail(email);
            yeniKullanici.setSifre(sifre);
            yeniKullanici.setTelefon(telefon);
            yeniKullanici.setRol(rol);
            
            // Kullanıcıyı kaydet (ID oluşsun)
            Kullanici kaydedilen = kullaniciService.kullaniciKaydet(yeniKullanici);

            // 2. Role Göre Alt Tabloyu Oluştur ve İsmi Kaydet
            if (rol == KullaniciRolu.IS_ARAYAN) {
                IsArayan isArayan = new IsArayan();
                isArayan.setKullanici(kaydedilen);
                
                // İsim soyisim ayrımı (Basit mantık)
                if (ad != null) {
                    if (ad.contains(" ")) {
                        int sonBosluk = ad.lastIndexOf(" ");
                        isArayan.setAd(ad.substring(0, sonBosluk));
                        isArayan.setSoyad(ad.substring(sonBosluk + 1));
                    } else {
                        isArayan.setAd(ad);
                        isArayan.setSoyad("");
                    }
                }
                isArayanRepository.save(isArayan);
                
            } else if (rol == KullaniciRolu.ISVEREN) {
                Isveren isveren = new Isveren();
                isveren.setKullanici(kaydedilen);
                isveren.setSirketAdi(ad); // İşveren için "ad" değişkeni Şirket Adı olur
                isverenRepository.save(isveren);
            }

            return ResponseEntity.ok(kaydedilen);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Geçersiz rol seçimi.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Kayıt başarısız: " + e.getMessage());
        }
    }

    // --- 3. PROFİL GÜNCELLE ---
    @PostMapping(value = "/profil/guncelle", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> mobildenProfilGuncelle(
            @RequestParam("id") Long id,
            @RequestParam(value = "ad", required = false) String ad,
            @RequestParam("telefon") String telefon,
            @RequestParam(value = "meslek", required = false) String meslek,
            @RequestParam(value = "ozetBilgi", required = false) String ozetBilgi,
            @RequestParam(value = "cvDosya", required = false) MultipartFile cvDosya,
            @RequestParam(value = "profilResmi", required = false) MultipartFile profilResmi,
            @RequestParam(value = "sirketAdi", required = false) String sirketAdi,
            @RequestParam(value = "webSitesi", required = false) String webSitesi,
            @RequestParam(value = "adres", required = false) String adres,
            @RequestParam(value = "hakkimizda", required = false) String hakkimizda,
            @RequestParam(value = "enlem", required = false) Double enlem,
            @RequestParam(value = "boylam", required = false) Double boylam,
            @RequestParam(value = "logoDosya", required = false) MultipartFile logoDosya
    ) {
        try {
            Optional<Kullanici> kullaniciOpt = kullaniciRepository.findById(id);
            if (kullaniciOpt.isEmpty()) return ResponseEntity.badRequest().body("Kullanıcı bulunamadı");
            Kullanici kullanici = kullaniciOpt.get();

            if (telefon != null) kullanici.setTelefon(telefon);
            kullaniciRepository.save(kullanici);

            if (kullanici.getRol() == KullaniciRolu.IS_ARAYAN) {
                Optional<IsArayan> isArayanOpt = isArayanRepository.findByKullaniciId(id);
                IsArayan isArayan = isArayanOpt.orElse(new IsArayan());
                if (isArayan.getKullanici() == null) isArayan.setKullanici(kullanici);

                if (ad != null) {
                    if (ad.contains(" ")) {
                        int sonBosluk = ad.lastIndexOf(" ");
                        isArayan.setAd(ad.substring(0, sonBosluk));
                        isArayan.setSoyad(ad.substring(sonBosluk + 1));
                    } else {
                        isArayan.setAd(ad);
                        isArayan.setSoyad("");
                    }
                }
                if (meslek != null) isArayan.setMeslek(meslek);
                if (ozetBilgi != null) isArayan.setOzet_bilgi(ozetBilgi);
                
                if (cvDosya != null && !cvDosya.isEmpty()) {
                    isArayan.setCvDosya(cvDosya.getBytes());
                    isArayan.setCvDosyaAdi(cvDosya.getOriginalFilename());
                }
                
                if (profilResmi != null && !profilResmi.isEmpty()) {
                    isArayan.setProfilResmi(profilResmi.getBytes());
                }
                isArayanRepository.save(isArayan);
            
            } else if (kullanici.getRol() == KullaniciRolu.ISVEREN) {
                Optional<Isveren> isverenOpt = isverenRepository.findByKullaniciId(id);
                Isveren isveren = isverenOpt.orElse(new Isveren());
                if (isveren.getKullanici() == null) isveren.setKullanici(kullanici);

                if (sirketAdi != null && !sirketAdi.isEmpty()) isveren.setSirketAdi(sirketAdi);
                else if (ad != null && !ad.isEmpty()) isveren.setSirketAdi(ad);

                if (webSitesi != null) isveren.setWebSitesi(webSitesi);
                if (adres != null) isveren.setAdres(adres);
                if (hakkimizda != null) isveren.setHakkimizda(hakkimizda);
                if (enlem != null) isveren.setEnlem(enlem);
                if (boylam != null) isveren.setBoylam(boylam);

                if (logoDosya != null && !logoDosya.isEmpty()) {
                    isveren.setLogo(logoDosya.getBytes());
                }
                isverenRepository.save(isveren);
            }
            return profilGetir(id);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Dosya hatası: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Hata: " + e.getMessage());
        }
    }

    // --- 4. PROFİL GETİR ---
    @GetMapping("/profil/{id}")
    public ResponseEntity<?> profilGetir(@PathVariable Long id) {
        Optional<Kullanici> kullaniciOpt = kullaniciRepository.findById(id);
        if (kullaniciOpt.isEmpty()) return ResponseEntity.badRequest().body("Kullanıcı yok.");

        Kullanici k = kullaniciOpt.get();
        Map<String, Object> response = new HashMap<>();

        response.put("id", k.getId());
        response.put("email", k.getEmail());
        response.put("telefon", k.getTelefon());
        response.put("rol", k.getRol());

        if (k.getRol() == KullaniciRolu.ISVEREN) {
            Optional<Isveren> isverenOpt = isverenRepository.findByKullaniciId(k.getId());
            if (isverenOpt.isPresent()) {
                Isveren i = isverenOpt.get();
                response.put("sirketAdi", i.getSirketAdi());
                response.put("webSitesi", i.getWebSitesi());
                response.put("hakkimizda", i.getHakkimizda());
                response.put("adres", i.getAdres());
                response.put("enlem", i.getEnlem());
                response.put("boylam", i.getBoylam());
                
                if(i.getLogo() != null) {
                    try {
                        byte[] resizedLogo = resizeImage(i.getLogo(), 200, 200); 
                        if (resizedLogo != null) {
                            response.put("logoBase64", Base64.getEncoder().encodeToString(resizedLogo));
                        }
                    } catch (Exception e) { response.put("logoBase64", null); }
                }
            }
        } 
        else if (k.getRol() == KullaniciRolu.IS_ARAYAN) {
            Optional<IsArayan> isArayanOpt = isArayanRepository.findByKullaniciId(k.getId());
            if (isArayanOpt.isPresent()) {
                IsArayan ia = isArayanOpt.get();
                String tamAd = (ia.getAd() != null ? ia.getAd() : "") + (ia.getSoyad() != null ? " " + ia.getSoyad() : "");
                response.put("ad", tamAd.trim());
                response.put("meslek", ia.getMeslek());
                response.put("ozetBilgi", ia.getOzet_bilgi());
                if(ia.getCvDosya() != null) {
                    response.put("cvDosyaAdi", ia.getCvDosyaAdi() != null ? ia.getCvDosyaAdi() : "cv.pdf");
                }
                
                if (ia.getProfilResmi() != null) {
                    try {
                        byte[] resized = resizeImage(ia.getProfilResmi(), 200, 200);
                        if (resized != null) {
                            response.put("profilResmiBase64", Base64.getEncoder().encodeToString(resized));
                        }
                    } catch (Exception e) { response.put("profilResmiBase64", null); }
                }
            }
        }
        return ResponseEntity.ok(response);
    }

    private byte[] resizeImage(byte[] originalImageBytes, int targetWidth, int targetHeight) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(originalImageBytes);
            BufferedImage originalImage = ImageIO.read(bais);
            if (originalImage == null) return null;

            Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
            BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            
            Graphics2D g2d = outputImage.createGraphics();
            g2d.drawImage(resultingImage, 0, 0, null);
            g2d.dispose();
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(outputImage, "jpg", baos);
            return baos.toByteArray();
        } catch (Exception e) { return null; }
    }

    @GetMapping("/bildirimler/{kullaniciId}")
    public ResponseEntity<?> kullaniciBildirimleri(@PathVariable Long kullaniciId) {
        // Düzeltilmiş repository metodunu kullan
        return ResponseEntity.ok(bildirimRepository.findByKullaniciIdOrderByTarihDesc(kullaniciId));
    }

    @DeleteMapping("/bildirim-sil/{id}")
    public ResponseEntity<?> bildirimSil(@PathVariable Long id) {
        if (bildirimRepository.existsById(id)) {
            bildirimRepository.deleteById(id);
            return ResponseEntity.ok("Silindi");
        }
        return ResponseEntity.badRequest().body("Bildirim bulunamadı");
    }
}