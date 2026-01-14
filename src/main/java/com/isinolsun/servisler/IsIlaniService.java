package com.isinolsun.servisler;

import com.isinolsun.depolar.BasvuruRepository;
import com.isinolsun.depolar.IsIlaniRepository;
import com.isinolsun.varliklar.IsIlani;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import java.util.List;

@Service
public class IsIlaniService {

    private final IsIlaniRepository isIlaniRepository;
    private final BasvuruRepository basvuruRepository;

    public IsIlaniService(IsIlaniRepository isIlaniRepository, BasvuruRepository basvuruRepository) {
        this.isIlaniRepository = isIlaniRepository;
        this.basvuruRepository = basvuruRepository;
    }

    public List<IsIlani> tumAktifIlanlariGetir() {
        return isIlaniRepository.findByAktifTrueOrderByYayinlanmaTarihiDesc();
    }

    // --- GELİŞMİŞ ARAMA VE SIRALAMA (VİTRİN ÖZELLİĞİ) ---
    public List<IsIlani> gelismisAra(String kelime, String sehir) {
        List<IsIlani> sonuc;
        
        // 1. Veritabanından Çekme İşlemi (Hata veren yerler düzeltildi)
        if (kelime != null && !kelime.isEmpty() && sehir != null && !sehir.isEmpty()) {
            sonuc = isIlaniRepository.findByBaslikContainingIgnoreCaseAndSehirContainingIgnoreCaseAndAktifTrueOrderByYayinlanmaTarihiDesc(kelime, sehir);
        } else if (kelime != null && !kelime.isEmpty()) {
            sonuc = isIlaniRepository.findByBaslikContainingIgnoreCaseAndAktifTrueOrderByYayinlanmaTarihiDesc(kelime);
        } else if (sehir != null && !sehir.isEmpty()) {
            sonuc = isIlaniRepository.findBySehirContainingIgnoreCaseAndAktifTrueOrderByYayinlanmaTarihiDesc(sehir);
        } else {
            sonuc = isIlaniRepository.findByAktifTrueOrderByYayinlanmaTarihiDesc();
        }

        // 2. Java Tarafında Sıralama (Vitrin İlanları En Üste)
        sonuc.sort((ilan1, ilan2) -> {
            boolean v1 = ilan1.isVitrinde();
            boolean v2 = ilan2.isVitrinde();
            
            // Eğer ilan1 vitrinde ise öne (-1), değilse arkaya (1)
            if (v1 && !v2) return -1;
            if (!v1 && v2) return 1;
            return 0; // Eşitse dokunma (Tarih sırası korunur)
        });
        
        return sonuc;
    }

    public IsIlani ilanKaydet(IsIlani isIlani) {
        return isIlaniRepository.save(isIlani);
    }

    // --- ŞEHRE GÖRE ARA ---
    public List<IsIlani> sehreGoreAra(String sehir) {
        return isIlaniRepository.findBySehirContainingIgnoreCaseAndAktifTrueOrderByYayinlanmaTarihiDesc(sehir);
    }

    // --- BAŞLIĞA GÖRE ARA ---
    public List<IsIlani> basligaGoreAra(String baslik) {
        // Burayı da 'OrderBy' olan metodla değiştirdim ki hata vermesin
        return isIlaniRepository.findByBaslikContainingIgnoreCaseAndAktifTrueOrderByYayinlanmaTarihiDesc(baslik);
    }
    
    public IsIlani ilanGetir(Long id) {
        return isIlaniRepository.findById(id).orElse(null);
    }

    // --- GÜVENLİ SİLME ---
    @Transactional
    public void ilanSil(Long id) {
        basvuruRepository.deleteByIsIlaniId(id);
        isIlaniRepository.deleteById(id);
    }

    public List<IsIlani> isvereninIlanlariniGetir(Long isverenId) {
        return isIlaniRepository.findByIsverenId(isverenId);
    }
}