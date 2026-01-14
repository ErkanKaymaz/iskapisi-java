package com.isinolsun.depolar;

import com.isinolsun.varliklar.Basvuru;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BasvuruRepository extends JpaRepository<Basvuru, Long> {
    
    // İlanın ID'sine göre başvuruları getir
    List<Basvuru> findByIsIlaniId(Long isIlaniId);
    
    // Adayın ID'sine göre başvuruları getir
    List<Basvuru> findByIsArayanId(Long isArayanId);
    
    // Başvuru kontrolü (Daha önce başvurmuş mu?)
    boolean existsByIsIlaniIdAndIsArayanId(Long isIlaniId, Long isArayanId);

    // Kullanıcının ID'sine göre yaptığı başvuruları getir (Panel için)
    List<Basvuru> findByIsArayanKullaniciId(Long kullaniciId);

    // --- YENİ EKLENEN (SİLME İŞLEMİ İÇİN ŞART) ---
    // Bir ilanı silmeden önce ona ait başvuruları silmek için
    void deleteByIsIlaniId(Long isIlaniId);
}