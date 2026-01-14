package com.isinolsun.depolar;

import com.isinolsun.varliklar.Favori;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional; // <-- BU EKLENDI

import java.util.List;
import java.util.Optional;

public interface FavoriRepository extends JpaRepository<Favori, Long> {
    
    // --- MEVCUT WEB METODLARI (AYNEN KALDI) ---
    // Bir kişinin favorilerini getir
    List<Favori> findByIsArayanIdOrderByEklenmeTarihiDesc(Long isArayanId);
    
    // Favori var mı kontrol et (Kalp dolu mu boş mu olacak?)
    boolean existsByIsArayanIdAndIsIlaniId(Long isArayanId, Long isIlaniId);
    
    // Silmek için bul (Web tarafı tek tek siliyor)
    Optional<Favori> findByIsArayanIdAndIsIlaniId(Long isArayanId, Long isIlaniId);

    // --- YENİ EKLENEN (MOBİL ADMIN SİLME İÇİN) ---
    // Bir ilan silindiğinde ona ait TÜM favorileri siler
    @Transactional // Toplu silme işlemi olduğu için bu şart
    void deleteByIsIlaniId(Long isIlaniId);
}