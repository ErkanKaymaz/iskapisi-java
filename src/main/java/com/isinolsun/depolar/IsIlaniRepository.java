package com.isinolsun.depolar;

import com.isinolsun.varliklar.IsIlani;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface IsIlaniRepository extends JpaRepository<IsIlani, Long> {

    // ==========================================
    // WEB TARAFI (MEVCUT YAPI - DOKUNULMADI)
    // ==========================================

    // 1. ANA SAYFA (Hepsini Getir - Sıralı)
    List<IsIlani> findByAktifTrueOrderByYayinlanmaTarihiDesc();

    // 2. HEM KELİME HEM ŞEHİR ARAMASI (Web için)
    List<IsIlani> findByBaslikContainingIgnoreCaseAndSehirContainingIgnoreCaseAndAktifTrueOrderByYayinlanmaTarihiDesc(String baslik, String sehir);

    // 3. SADECE KELİME (BAŞLIK) ARAMASI (Web için)
    List<IsIlani> findByBaslikContainingIgnoreCaseAndAktifTrueOrderByYayinlanmaTarihiDesc(String baslik);

    // 4. SADECE ŞEHİR ARAMASI (Web için)
    List<IsIlani> findBySehirContainingIgnoreCaseAndAktifTrueOrderByYayinlanmaTarihiDesc(String sehir);

    // 5. İŞVERENİN İLANLARI
    List<IsIlani> findByIsverenId(Long isverenId);
    
    // ==========================================
    // MOBİL TARAFI (GÜNCELLENEN AKILLI SORGULAR)
    // ==========================================

    // 1. Mobil Anasayfa (Sponsorlu üstte, sonra tarih sırası)
    @Query("SELECT i FROM IsIlani i WHERE i.aktif = true " +
            "ORDER BY (CASE WHEN (i.vitrinBitisTarihi IS NOT NULL AND i.vitrinBitisTarihi > CURRENT_TIMESTAMP) THEN 1 ELSE 0 END) DESC, " +
            "i.yayinlanmaTarihi DESC")
     List<IsIlani> mobilIcinOzelSiraliGetir();

     // 2. MOBİL GELİŞMİŞ ARAMA (DÜZELTİLEN KISIM)
     // Mantık: (Kelime varsa Ara) VE (Şehir varsa Ara)
     // Kelimeyi hem "Başlık"ta hem "Şirket Adı"nda arar.
     @Query("SELECT i FROM IsIlani i WHERE i.aktif = true AND " +
            "(:kelime IS NULL OR :kelime = '' OR (LOWER(i.baslik) LIKE LOWER(CONCAT('%', :kelime, '%')) OR LOWER(i.isveren.sirketAdi) LIKE LOWER(CONCAT('%', :kelime, '%')))) AND " +
            "(:sehir IS NULL OR :sehir = '' OR LOWER(i.sehir) LIKE LOWER(CONCAT('%', :sehir, '%'))) " +
            "ORDER BY (CASE WHEN (i.vitrinBitisTarihi IS NOT NULL AND i.vitrinBitisTarihi > CURRENT_TIMESTAMP) THEN 1 ELSE 0 END) DESC, " +
            "i.yayinlanmaTarihi DESC")
     List<IsIlani> mobilGelismisArama(@Param("kelime") String kelime, @Param("sehir") String sehir);
}