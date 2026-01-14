package com.isinolsun.depolar;

import com.isinolsun.varliklar.IsArayan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; // BU IMPORT ŞART

public interface IsArayanRepository extends JpaRepository<IsArayan, Long> {
    // Eskiden: IsArayan findByKullaniciId(Long kullaniciId);
    // YENİSİ (Hata Çözen):
    Optional<IsArayan> findByKullaniciId(Long kullaniciId);
}