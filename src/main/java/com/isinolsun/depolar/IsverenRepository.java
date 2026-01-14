package com.isinolsun.depolar;

import com.isinolsun.varliklar.Isveren;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; // BU IMPORT ŞART

public interface IsverenRepository extends JpaRepository<Isveren, Long> {
    // Eskiden: Isveren findByKullaniciId(Long kullaniciId);
    // YENİSİ (Hata Çözen):
    Optional<Isveren> findByKullaniciId(Long kullaniciId);
}