package com.isinolsun.depolar;

import com.isinolsun.tipler.KullaniciRolu;
import com.isinolsun.varliklar.Kullanici;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface KullaniciRepository extends JpaRepository<Kullanici, Long> {
    
    // --- MEVCUT WEB TARAFININ KULLANDIKLARI (DOKUNULMADI) ---
    Kullanici findByEmailAndSifre(String email, String sifre);
    
    boolean existsByRol(KullaniciRolu rol);

    Optional<Kullanici> findByEmail(String email);

    // --- YENİ EKLENEN (MOBİL ADMIN İÇİN) ---
    // Bu metod eksik olduğu için "undefined" hatası alıyordun.
    // Spring Boot bu ismi görünce otomatik olarak o roldeki kullanıcıları sayar.
    long countByRol(KullaniciRolu rol);
}