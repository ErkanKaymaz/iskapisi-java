package com.isinolsun.servisler;

import com.isinolsun.depolar.BasvuruRepository;
import com.isinolsun.dto.MobilBasvuruDTO;
import com.isinolsun.tipler.BasvuruDurumu;
import com.isinolsun.varliklar.Basvuru;
import com.isinolsun.varliklar.Bildirim;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BasvuruService {

    private final BasvuruRepository basvuruRepository;
    private final BildirimService bildirimService;

    public BasvuruService(BasvuruRepository basvuruRepository, BildirimService bildirimService) {
        this.basvuruRepository = basvuruRepository;
        this.bildirimService = bildirimService;
    }

    // ============================================================
    // WEB TARAFI İÇİN OLAN METODLAR (DOKUNULMADI - AYNI KALDI)
    // ============================================================

    public Basvuru basvuruYap(Basvuru basvuru) {
        if(basvuru.getIsIlani() != null && basvuru.getIsArayan() != null) {
            boolean zatenBasvurduMu = basvuruRepository.existsByIsIlaniIdAndIsArayanId(
                    basvuru.getIsIlani().getId(), 
                    basvuru.getIsArayan().getId()
            );
            if (zatenBasvurduMu) {
                throw new RuntimeException("Bu ilana zaten başvurdunuz!");
            }
        }
        return basvuruRepository.save(basvuru);
    }

    public List<Basvuru> ilanaGelenBasvurular(Long ilanId) {
        return basvuruRepository.findByIsIlaniId(ilanId);
    }
    
    public Basvuru basvuruGetir(Long id) {
        return basvuruRepository.findById(id).orElse(null);
    }

    public List<Long> kullanicininBasvurduguIlanIdleri(Long kullaniciId) {
        List<Basvuru> basvurular = basvuruRepository.findByIsArayanKullaniciId(kullaniciId);
        return basvurular.stream()
                .map(basvuru -> basvuru.getIsIlani().getId())
                .collect(Collectors.toList());
    }

    public List<Basvuru> kullanicininBasvurulariniGetir(Long kullaniciId) {
        return basvuruRepository.findByIsArayanKullaniciId(kullaniciId);
    }

    // ============================================================
    // MOBİL İÇİN DTO DÖNÜŞÜMÜ YAPAN METODLAR (GÜNCELLENDİ)
    // ============================================================
    
    // 1. Bir ilana yapılan başvuruları (Aday detaylarıyla) getirir
    public List<MobilBasvuruDTO> getMobilIlanBasvurulari(Long ilanId) {
        List<Basvuru> basvurular = basvuruRepository.findByIsIlaniId(ilanId);
        return mapToMobilDTO(basvurular);
    }

    // 2. Bir kullanıcının yaptığı başvuruları getirir
    public List<MobilBasvuruDTO> getMobilKullaniciBasvurulari(Long kullaniciId) {
        List<Basvuru> basvurular = basvuruRepository.findByIsArayanKullaniciId(kullaniciId);
        return mapToMobilDTO(basvurular);
    }

    // ORTAK DÖNÜŞTÜRÜCÜ (Kod tekrarını önler ve hatayı engeller)
    private List<MobilBasvuruDTO> mapToMobilDTO(List<Basvuru> basvurular) {
        return basvurular.stream()
            .map(b -> new MobilBasvuruDTO(
                b.getId(),
                b.getIsIlani().getId(),
                b.getIsIlani().getBaslik(),
                b.getIsIlani().getIsveren().getSirketAdi(),
                b.getIsIlani().getSehir(),
                b.getDurum(),
                b.getBasvuruTarihi(),   
                b.getMulakatTarihi(),   
                b.getIsverenNotu(),
                
                // --- İŞTE EKSİK OLAN ADAY BİLGİLERİ BURADA EKLENİYOR ---
                b.getIsArayan().getAd(),
                b.getIsArayan().getSoyad(),
                b.getIsArayan().getMeslek(),
                b.getIsArayan().getOzet_bilgi(), // Entity'de getOzet_bilgi ise böyle kalır
                b.getIsArayan().getKullanici().getTelefon(),
                b.getIsArayan().getKullanici().getEmail(),
                (b.getIsArayan().getCvDosya() != null) // CV var mı?
            ))
            .collect(Collectors.toList());
    }

    // ============================================================
    // DURUM GÜNCELLEME (AYNI KALDI)
    // ============================================================
    public void durumGuncelle(Long basvuruId, BasvuruDurumu yeniDurum, String mesaj, LocalDateTime tarih) {
        Basvuru basvuru = basvuruRepository.findById(basvuruId).orElse(null);
        if (basvuru != null) {
            basvuru.setDurum(yeniDurum);
            basvuru.setIsverenNotu(mesaj);
            if (tarih != null) basvuru.setMulakatTarihi(tarih);
            
            basvuruRepository.save(basvuru);

            try {
                Bildirim bildirim = new Bildirim();
                bildirim.setKullanici(basvuru.getIsArayan().getKullanici());
                
                String ilanAdi = basvuru.getIsIlani().getBaslik();
                String bildirimMesaji = "";

                if (yeniDurum == BasvuruDurumu.MULAKAT && tarih != null) {
                    String tarihStr = tarih.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
                    bildirimMesaji = "📅 MÜLAKAT: '" + ilanAdi + "' için davet! Tarih: " + tarihStr;
                } else if (yeniDurum == BasvuruDurumu.KABUL_EDILDI) {
                    bildirimMesaji = "🎉 TEBRİKLER! '" + ilanAdi + "' başvurunuz KABUL EDİLDİ!";
                } else if (yeniDurum == BasvuruDurumu.REDDEDILDI) {
                    bildirimMesaji = "❌ '" + ilanAdi + "' başvurunuz olumsuz sonuçlandı.";
                }

                if (!bildirimMesaji.isEmpty()) {
                    bildirim.setMesaj(bildirimMesaji);
                    bildirimService.kaydet(bildirim);
                }
            } catch (Exception e) {
                System.out.println("Bildirim gönderilirken hata: " + e.getMessage());
            }
        }
    }
}