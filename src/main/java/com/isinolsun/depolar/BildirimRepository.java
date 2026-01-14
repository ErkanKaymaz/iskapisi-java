package com.isinolsun.depolar;

import com.isinolsun.varliklar.Bildirim;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BildirimRepository extends JpaRepository<Bildirim, Long> {
    // Bir kullanıcıya ait bildirimleri getir
    List<Bildirim> findByKullaniciIdOrderByTarihDesc(Long kullaniciId);
}