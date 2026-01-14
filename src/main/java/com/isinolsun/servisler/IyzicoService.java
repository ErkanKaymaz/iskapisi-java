package com.isinolsun.servisler;

import com.iyzipay.Options;
import com.iyzipay.model.*;
import com.iyzipay.request.CreatePaymentRequest;
import com.isinolsun.varliklar.Kullanici;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class IyzicoService {

    // --- IYZICO SANDBOX ANAHTARLARI ---
    // BULDUĞUN UZUN KODLARI TIRNAK İÇLERİNE YAPIŞTIR 👇
    private final String API_KEY = "sandbox-00FIP7W8yfsKeo86kWmPuWWhjamdtgNY";
    private final String SECRET_KEY = "sandbox-Wkc7vWAmAz9bSCJHbrKb6oFDLSiLd4Mj";
    
    private final String BASE_URL = "https://sandbox-api.iyzipay.com";

    public boolean odemeYap(Kullanici kullanici, double fiyat, String kartSahibi, String kartNo, String ay, String yil, String cvv) {
        
        try {
            Options options = new Options();
            options.setApiKey(API_KEY);
            options.setSecretKey(SECRET_KEY);
            options.setBaseUrl(BASE_URL);

            CreatePaymentRequest request = new CreatePaymentRequest();
            request.setLocale(Locale.TR.getValue());
            request.setConversationId("123456789");
            request.setPrice(new BigDecimal(fiyat));
            request.setPaidPrice(new BigDecimal(fiyat));
            request.setCurrency(Currency.TRY.name());
            request.setInstallment(1);
            request.setBasketId("B67832");
            request.setPaymentChannel(PaymentChannel.WEB.name());
            request.setPaymentGroup(PaymentGroup.PRODUCT.name());

            // --- KART BİLGİLERİ ---
            PaymentCard paymentCard = new PaymentCard();
            paymentCard.setCardHolderName(kartSahibi);
            paymentCard.setCardNumber(kartNo.replaceAll("\\s+", ""));
            paymentCard.setExpireMonth(ay);
            if (yil.length() == 2) {
                paymentCard.setExpireYear("20" + yil);
            } else {
                paymentCard.setExpireYear(yil);
            }
            paymentCard.setCvc(cvv);
            paymentCard.setRegisterCard(0);
            request.setPaymentCard(paymentCard);

            // --- ALICI BİLGİLERİ (HATA VEREN KISIM DÜZELTİLDİ) ---
            Buyer buyer = new Buyer();
            buyer.setId(kullanici.getId().toString());
            
            // Kullanici nesnesinde getAd() yok, o yüzden "Sayın Yetkili" yazıyoruz.
            // Iyzico Sandbox isim kontrolü yapmaz, sorun çıkmaz.
            buyer.setName("Sayın"); 
            buyer.setSurname("Yetkili");
            
            buyer.setGsmNumber(kullanici.getTelefon() != null ? kullanici.getTelefon() : "+905555555555");
            buyer.setEmail(kullanici.getEmail());
            buyer.setIdentityNumber("11111111111");
            buyer.setLastLoginDate("2025-01-01 12:00:00");
            buyer.setRegistrationDate("2025-01-01 12:00:00");
            buyer.setRegistrationAddress("Istanbul");
            buyer.setIp("85.34.78.112");
            buyer.setCity("Istanbul");
            buyer.setCountry("Turkey");
            buyer.setZipCode("34732");
            request.setBuyer(buyer);

            // --- ADRES BİLGİLERİ ---
            Address billingAddress = new Address();
            billingAddress.setContactName(buyer.getName());
            billingAddress.setCity("Istanbul");
            billingAddress.setCountry("Turkey");
            billingAddress.setAddress("Test Adresi");
            billingAddress.setZipCode("34742");
            request.setBillingAddress(billingAddress);
            request.setShippingAddress(billingAddress);

            // --- SEPET İÇERİĞİ ---
            BasketItem firstBasketItem = new BasketItem();
            firstBasketItem.setId("BI101");
            firstBasketItem.setName("Vitrin Paketi"); // setName kullanıldı
            firstBasketItem.setCategory1("Reklam");
            firstBasketItem.setItemType(BasketItemType.VIRTUAL.name());
            firstBasketItem.setPrice(new BigDecimal(fiyat));
            
            List<BasketItem> basketItems = new ArrayList<>();
            basketItems.add(firstBasketItem);
            request.setBasketItems(basketItems);

            // --- IYZICO'YA GÖNDER ---
            Payment payment = Payment.create(request, options);

            // SONUÇ NE?
            if ("success".equalsIgnoreCase(payment.getStatus())) {
                return true; // Ödeme Başarılı
            } else {
                System.out.println("IYZICO HATA: " + payment.getErrorMessage());
                return false; // Ödeme Başarısız
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}