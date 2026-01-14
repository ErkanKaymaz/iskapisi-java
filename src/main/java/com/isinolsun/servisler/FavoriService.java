package com.isinolsun.servisler;

import com.isinolsun.depolar.FavoriRepository;
import com.isinolsun.varliklar.Favori;
import com.isinolsun.varliklar.IsArayan;
import com.isinolsun.varliklar.IsIlani;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class FavoriService {

    private final FavoriRepository favoriRepository;

    public FavoriService(FavoriRepository favoriRepository) {
        this.favoriRepository = favoriRepository;
    }

    // Toggle mantığı: Varsa siler, yoksa ekler
    public boolean favoriIslemi(IsArayan isArayan, IsIlani ilan) {
        Optional<Favori> mevcut = favoriRepository.findByIsArayanIdAndIsIlaniId(isArayan.getId(), ilan.getId());
        
        if (mevcut.isPresent()) {
            favoriRepository.delete(mevcut.get());
            return false; // Silindi (Kalp boşalacak)
        } else {
            Favori yeni = new Favori();
            yeni.setIsArayan(isArayan);
            yeni.setIsIlani(ilan);
            favoriRepository.save(yeni);
            return true; // Eklendi (Kalp dolacak)
        }
    }

    public List<Favori> getKullaniciFavorileri(Long isArayanId) {
        return favoriRepository.findByIsArayanIdOrderByEklenmeTarihiDesc(isArayanId);
    }

    public boolean favoriMi(Long isArayanId, Long ilanId) {
        return favoriRepository.existsByIsArayanIdAndIsIlaniId(isArayanId, ilanId);
    }
}