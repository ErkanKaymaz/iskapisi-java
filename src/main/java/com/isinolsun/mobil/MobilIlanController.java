package com.isinolsun.mobil;

import com.isinolsun.depolar.IsIlaniRepository;
import com.isinolsun.dto.MobilIlanDTO;
import com.isinolsun.servisler.IsIlaniService;
import com.isinolsun.varliklar.IsIlani;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mobil/ilanlar")
@CrossOrigin(origins = "*") 
public class MobilIlanController {

    @Autowired
    private IsIlaniService isIlaniService;

    @Autowired
    private IsIlaniRepository isIlaniRepository; 

    // --- 1. İLAN LİSTESİ GETİR (Sponsorlu Üstte + Resimler Küçültülmüş) ---
    @GetMapping
    public ResponseEntity<List<MobilIlanDTO>> ilanlariGetir(
            @RequestParam(required = false) String kelime,
            @RequestParam(required = false) String sehir) {

        System.out.println(">>> MOBİL: İlan listesi isteniyor... Kelime: " + kelime + ", Şehir: " + sehir);

        List<IsIlani> hamIlanlar;

        // DÜZELTME BURADA YAPILDI
        if ((kelime != null && !kelime.isEmpty()) || (sehir != null && !sehir.isEmpty())) {
            // Arama varsa: Yeni yazdığımız "VE" mantıklı sorguyu çağırıyoruz
            // Garson VE Ankara ise sadece Ankara'dakileri getirir.
            hamIlanlar = isIlaniRepository.mobilGelismisArama(kelime, sehir);
        } else {
            // Arama yoksa: Ana sayfa sıralaması (Sponsorlu üstte)
            hamIlanlar = isIlaniRepository.mobilIcinOzelSiraliGetir();
        }

        List<MobilIlanDTO> mobilIlanlar = hamIlanlar.stream()
            .map(ilan -> {
                String sirketAdi = "Belirtilmemiş";
                Double enlem = null;
                Double boylam = null;
                byte[] logoBytes = null;

                if (ilan.getIsveren() != null) {
                    if (ilan.getIsveren().getSirketAdi() != null) {
                        sirketAdi = ilan.getIsveren().getSirketAdi();
                    }
                    enlem = ilan.getIsveren().getEnlem();
                    boylam = ilan.getIsveren().getBoylam();
                    logoBytes = ilan.getIsveren().getLogo();
                }
                
                // Resim Sıkıştırma (Liste için 150x150)
                byte[] resizedLogo = resizeImage(logoBytes, 150, 150);

                return new MobilIlanDTO(
                    ilan.getId(),
                    ilan.getBaslik(),
                    sirketAdi,
                    ilan.getSehir(),
                    ilan.getMaas(),
                    ilan.getYayinlanmaTarihi(),
                    resizedLogo,
                    ilan.getAciklama(),
                    enlem,
                    boylam,
                    ilan.getVitrinBitisTarihi()
                );
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(mobilIlanlar);
    }

    // --- 2. İLAN DETAY GETİR (DOKUNULMADI) ---
    @GetMapping("/{id}")
    public ResponseEntity<MobilIlanDTO> ilanDetay(@PathVariable Long id) {
        IsIlani ilan = isIlaniService.ilanGetir(id);

        if (ilan != null) {
            String sirketAdi = "Belirtilmemiş";
            Double enlem = null;
            Double boylam = null;
            byte[] logoBytes = null;

            if (ilan.getIsveren() != null) {
                sirketAdi = ilan.getIsveren().getSirketAdi();
                enlem = ilan.getIsveren().getEnlem();
                boylam = ilan.getIsveren().getBoylam();
                logoBytes = ilan.getIsveren().getLogo();
            }

            // Detay için orta boy resim (400x400)
            byte[] resizedLogo = resizeImage(logoBytes, 400, 400);

            MobilIlanDTO detay = new MobilIlanDTO(
                ilan.getId(),
                ilan.getBaslik(),
                sirketAdi,
                ilan.getSehir(),
                ilan.getMaas(),
                ilan.getYayinlanmaTarihi(),
                resizedLogo,
                ilan.getAciklama(),
                enlem,
                boylam,
                ilan.getVitrinBitisTarihi()
            );
            return ResponseEntity.ok(detay);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // --- RESİM SIKIŞTIRMA METODU (DOKUNULMADI) ---
    private byte[] resizeImage(byte[] originalImageBytes, int targetWidth, int targetHeight) {
        if (originalImageBytes == null || originalImageBytes.length == 0) return null;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(originalImageBytes);
            BufferedImage originalImage = ImageIO.read(bais);
            if (originalImage == null) return null;

            BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics2D = resizedImage.createGraphics();
            graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
            graphics2D.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            if (!writers.hasNext()) return null;
            
            ImageWriter writer = writers.next();
            ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
            writer.setOutput(ios);

            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(0.7f);
            }
            writer.write(null, new javax.imageio.IIOImage(resizedImage, null, null), param);
            writer.dispose();
            ios.close();
            return baos.toByteArray();
        } catch (Exception e) {
            System.err.println("Resim hatası: " + e.getMessage());
            return null;
        }
    }
}