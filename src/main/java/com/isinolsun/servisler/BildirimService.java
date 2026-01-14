package com.isinolsun.servisler;

import com.isinolsun.depolar.BildirimRepository;
import com.isinolsun.varliklar.Bildirim;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BildirimService {

    private final BildirimRepository bildirimRepository;

    public BildirimService(BildirimRepository bildirimRepository) {
        this.bildirimRepository = bildirimRepository;
    }

    public void kaydet(Bildirim bildirim) {
        bildirimRepository.save(bildirim);
    }

    public List<Bildirim> kullaniciBildirimleri(Long kullaniciId) {
        return bildirimRepository.findByKullaniciIdOrderByTarihDesc(kullaniciId);
    }

    public void sil(Long id) {
        bildirimRepository.deleteById(id);
    }
}