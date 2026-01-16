package com.isinolsun.servisler;

import com.isinolsun.dto.MailDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class MailService {

    private final WebClient webClient;

    // --- DEĞİŞİKLİK BURADA ---
    // Artık dışarıdan Builder beklemiyoruz, kendimiz oluşturuyoruz.
    // Bu sayede "Bean bulunamadı" hatası kökten çözülüyor.
    public MailService() {
        this.webClient = WebClient.builder()
                .baseUrl("http://127.0.0.1:8000")
                .build();
    }
    // -------------------------

    public void mailGonder(String email, String baslik, String mesaj) {
        // DTO'yu doldur
        MailDTO dto = new MailDTO(email, baslik, mesaj);

        System.out.println("Mail servisine istek atılıyor: " + email);

        // Python servisine POST isteği at
        webClient.post()
                .uri("/send-mail") // Python'daki @app.post("/send-mail") kısmı
                .bodyValue(dto)     // Veri kutusunu içine koy
                .retrieve()         // Cevabı al
                .bodyToMono(String.class)
                .doOnSuccess(res -> System.out.println("✅ PYTHON CEVABI: " + res))
                .doOnError(err -> System.err.println("❌ MAIL HATASI: " + err.getMessage()))
                .subscribe(); // İşlemi başlat (Tetiği çek)
    }
}