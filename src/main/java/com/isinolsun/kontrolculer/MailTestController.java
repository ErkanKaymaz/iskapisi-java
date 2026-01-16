package com.isinolsun.kontrolculer;

import com.isinolsun.servisler.MailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MailTestController {

    private final MailService mailService;

    public MailTestController(MailService mailService) {
        this.mailService = mailService;
    }

    // Tarayıcıdan: http://localhost:8080/mail-test?email=senin@mailin.com yazınca çalışacak
    @GetMapping("/mail-test")
    public String denemeMailiAt(@RequestParam String email) {
        mailService.mailGonder(email, "Java Test Başlığı", "Merhaba, bu mail Spring Boot üzerinden Python servisi kullanılarak atıldı!");
        return "Mail gönderme emri verildi! Konsolu (Logları) kontrol et.";
    }
}