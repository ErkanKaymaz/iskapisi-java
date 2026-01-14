package com.isinolsun.servisler;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.*;
import java.util.*;

@Service
public class GeminiService {

    // 1. BURAYA YENİ ALDIĞIN API KEY'İ YAPIŞTIR (Eskisiyle karışmasın)
    private final String API_KEY = "AIzaSyDDnRESIWX8lNDBaZ8xTn3b0BhM3tb-kf0"; 
    
    // 2. İŞTE ÇÖZÜM BURADA: Senin bulduğun 'gemini-3-flash-preview' modelini yazdık.
    // Artık 404 hatası vermeyecek çünkü Google'ın sitesinde yazan kod bu.
    private final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent?key=" + API_KEY;

    public String kariyerAnaliziOlustur(String meslek, String ozgecmisAciklamasi, String cvMetni) {
        // Null kontrolü (Hata vermemesi için boş string atıyoruz)
        String safeMeslek = (meslek != null) ? meslek : "Belirtilmemiş";
        String safeOzgecmis = (ozgecmisAciklamasi != null) ? ozgecmisAciklamasi : "";
        String safeCv = (cvMetni != null) ? cvMetni : "";

        // Prompt Hazırlığı
        String prompt = "Sen bir kariyer koçusun. Şu profile göre Türkçe tavsiyeler ver: \n" +
                        "Meslek: " + safeMeslek + "\n" +
                        "Özet: " + safeOzgecmis + "\n" +
                        "CV Detayı: " + safeCv + "\n" +
                        "Lütfen kısa, maddeli ve motive edici konuş.";

        return yapayZekayaSor(prompt);
    }

    public String yapayZekayaSor(String metin) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 1. JSON GÖVDESİNİ MAP İLE OLUŞTURUYORUZ (En güvenli yöntem)
            Map<String, Object> part = new HashMap<>();
            part.put("text", metin);

            Map<String, Object> content = new HashMap<>();
            content.put("parts", Collections.singletonList(part));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", Collections.singletonList(content));

            // 2. İSTEĞİ GÖNDERİYORUZ
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(API_URL, HttpMethod.POST, entity, Map.class);

            // 3. CEVABI AYIKLIYORUZ
            Map<String, Object> responseBody = response.getBody();
            
            if (responseBody != null && responseBody.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> contentMap = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");
                    return (String) parts.get(0).get("text");
                }
            }
            return "Yapay zeka cevap üretemedi (Boş yanıt).";

        } catch (HttpClientErrorException e) {
            // HATA DETAYINI KONSOLA BAS
            System.err.println("--- GOOGLE API HATASI ---");
            System.err.println("Hata Kodu: " + e.getStatusCode());
            System.err.println("Mesaj: " + e.getResponseBodyAsString()); // Asıl hatayı burada göreceğiz
            System.err.println("-------------------------");
            
            return "Hata: " + e.getStatusCode() + " - Lütfen konsola bakıp hatayı kontrol et.";
            
        } catch (Exception e) {
            System.err.println("GENEL HATA: " + e.getMessage());
            e.printStackTrace();
            return "Sistem hatası: " + e.getMessage();
        }
    }
 // --- YENİ EKLENEN: İŞ İLANI OLUŞTURUCU ---
    public String ilanMetniOlustur(String baslik, String sirketAdi) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Sen profesyonel bir İnsan Kaynakları uzmanısın. ");
        prompt.append(sirketAdi).append(" isimli şirket için '").append(baslik).append("' pozisyonuna iş ilanı metni hazırla.\n");
        prompt.append("Lütfen şu başlıkları kullanarak HTML formatında (<b>, <br> kullanarak) yaz:\n");
        prompt.append("1. İş Tanımı\n");
        prompt.append("2. Aranan Nitelikler (Maddeler halinde)\n");
        prompt.append("3. Biz Ne Sunuyoruz?\n");
        prompt.append("Samimi ama profesyonel bir dil kullan. Kısa ve net olsun.");

        return yapayZekayaSor(prompt.toString());
    }
    public String ilanMetniOlusturMobil(String baslik, String sirketAdi) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Sen profesyonel bir İnsan Kaynakları uzmanısın. ");
        prompt.append(sirketAdi).append(" isimli şirket için '").append(baslik).append("' pozisyonuna iş ilanı metni hazırla.\n");
        // MOBİL FARK: HTML etiketi yasak, emoji ve madde işareti serbest
        prompt.append("Lütfen HTML etiketi (<b>, <br> vs.) KULLANMA.\n");
        prompt.append("Başlıkları büyük harfle yaz, maddeler için tire (-) kullan.\n");
        prompt.append("1. İŞ TANIMI\n");
        prompt.append("2. ARANAN NİTELİKLER\n");
        prompt.append("3. BİZ NE SUNUYORUZ?\n");
        prompt.append("Samimi, profesyonel ve motive edici olsun.");

        return yapayZekayaSor(prompt.toString());
    }
}