document.addEventListener('DOMContentLoaded', function() {
    
    // ==========================================
    // 1. NAVBAR SCROLL EFEKTİ
    // ==========================================
    const navbar = document.querySelector('.navbar');
    if (navbar) {
        window.addEventListener('scroll', () => {
            if (window.scrollY > 50) {
                navbar.classList.add('scrolled');
            } else {
                navbar.classList.remove('scrolled');
            }
        });
    }

    // ==========================================
    // 2. KART ANİMASYONLARI (FADE IN)
    // ==========================================
    const cards = document.querySelectorAll('.card');
    cards.forEach((card, index) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        card.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
        
        setTimeout(() => {
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, 100 * index);
    });

    // ==========================================
    // 3. İLAN DETAY SAYFASI: HARİTA GÖSTERİMİ
    // ==========================================
    const mapShowElement = document.getElementById('mapShow');
    if (mapShowElement && typeof L !== 'undefined') {
        const lat = mapShowElement.dataset.lat;
        const lng = mapShowElement.dataset.lng;
        const companyName = mapShowElement.dataset.company || "Konum";

        if (lat && lng) {
            const map = L.map('mapShow').setView([lat, lng], 15);
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                attribution: '© OpenStreetMap contributors'
            }).addTo(map);

            L.marker([lat, lng]).addTo(map)
                .bindPopup(`<b>${companyName}</b><br>Konum burası.`)
                .openPopup();
        }
    }

    // ==========================================
    // 4. PROFİL SAYFASI: KONUM SEÇİM HARİTASI
    // ==========================================
    const mapSelectElement = document.getElementById('mapSelect');
    if (mapSelectElement && typeof L !== 'undefined') {
        // Varsayılan: İstanbul
        let defaultLat = 41.0082;
        let defaultLng = 28.9784;

        // Formdaki gizli inputlardan değerleri al
        const latInput = document.getElementById('latInput');
        const lngInput = document.getElementById('lngInput');

        if (latInput.value && lngInput.value) {
            defaultLat = parseFloat(latInput.value);
            defaultLng = parseFloat(lngInput.value);
        } else {
            // Değer yoksa inputları varsayılanla doldur
            latInput.value = defaultLat;
            lngInput.value = defaultLng;
        }

        // Haritayı Başlat
        const map = L.map('mapSelect').setView([defaultLat, defaultLng], 13);
        
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors'
        }).addTo(map);

        // Sürüklenebilir Pin Ekle
        const marker = L.marker([defaultLat, defaultLng], { draggable: true }).addTo(map);

        // Olay: Pin sürüklendiğinde inputları güncelle
        marker.on('dragend', function(e) {
            const coord = e.target.getLatLng();
            latInput.value = coord.lat;
            lngInput.value = coord.lng;
        });

        // Olay: Haritaya tıklandığında pini oraya taşı
        map.on('click', function(e) {
            marker.setLatLng(e.latlng);
            latInput.value = e.latlng.lat;
            lngInput.value = e.latlng.lng;
        });
        
        // Harita tam yüklensin diye resize tetikle (Modal içindeyse bozulmasın diye)
        setTimeout(() => { map.invalidateSize(); }, 100);
    }

    // ==========================================
    // 5. ADMIN PANELİ: KULLANICI FİLTRELEME
    // ==========================================
    const kullaniciInput = document.getElementById('kullaniciAra');
    const rolSelect = document.getElementById('rolFiltre');
    const kullaniciTablo = document.getElementById('kullaniciTablosu');

    if (kullaniciInput && rolSelect && kullaniciTablo) {
        function filtreleKullanici() {
            const yaziDegeri = kullaniciInput.value.toUpperCase();
            const rolDegeri = rolSelect.value.toUpperCase();
            const satirlar = kullaniciTablo.getElementsByTagName('tr');

            for (let i = 1; i < satirlar.length; i++) { // Başlığı atla (i=1)
                const emailSutun = satirlar[i].getElementsByTagName('td')[1];
                const rolSutun = satirlar[i].getElementsByTagName('td')[2];

                if (emailSutun && rolSutun) {
                    const emailTxt = emailSutun.textContent || emailSutun.innerText;
                    const rolTxt = rolSutun.textContent || rolSutun.innerText;
                    
                    const yaziUyuyor = emailTxt.toUpperCase().indexOf(yaziDegeri) > -1;
                    const rolUyuyor = (rolDegeri === '' || rolDegeri === 'TÜM ROLLER') || (rolTxt.toUpperCase().indexOf(rolDegeri) > -1);
                    
                    satirlar[i].style.display = (yaziUyuyor && rolUyuyor) ? "" : "none";
                }
            }
        }
        kullaniciInput.addEventListener('keyup', filtreleKullanici);
        rolSelect.addEventListener('change', filtreleKullanici);
    }

    // ==========================================
    // 6. ADMIN PANELİ: İLAN FİLTRELEME
    // ==========================================
    const ilanInput = document.getElementById('ilanAra');
    const ilanTablo = document.getElementById('ilanTablosu');
    
    if (ilanInput && ilanTablo) {
        ilanInput.addEventListener('keyup', function() {
            const filter = ilanInput.value.toUpperCase();
            const rows = ilanTablo.getElementsByTagName('tr');
            for (let i = 1; i < rows.length; i++) {
                const text = rows[i].textContent || rows[i].innerText;
                rows[i].style.display = text.toUpperCase().indexOf(filter) > -1 ? "" : "none";
            }
        });
    }

    // ==========================================
    // 7. ADMIN PANELİ: GRAFİKLER (CHART.JS)
    // ==========================================
    const userChartCanvas = document.getElementById('userChart');
    const cityChartCanvas = document.getElementById('cityChart');

    if (userChartCanvas && typeof Chart !== 'undefined') {
        const isverenSayisi = userChartCanvas.dataset.isveren;
        const isArayanSayisi = userChartCanvas.dataset.isarayan;

        new Chart(userChartCanvas, {
            type: 'doughnut',
            data: {
                labels: ['İşveren', 'İş Arayan'],
                datasets: [{
                    data: [isverenSayisi, isArayanSayisi],
                    backgroundColor: ['#3b82f6', '#10b981'],
                    borderWidth: 0
                }]
            },
            options: { responsive: true, plugins: { legend: { position: 'bottom' } } }
        });
    }

    if (cityChartCanvas && typeof Chart !== 'undefined') {
        // Backend'den gelen String array formatını temizle: [Adana, Ankara] -> Adana, Ankara
        let labelsRaw = cityChartCanvas.dataset.labels;
        let countsRaw = cityChartCanvas.dataset.counts;
        
        // Köşeli parantezleri kaldır ve virgülden ayır
        const labels = labelsRaw ? labelsRaw.replace('[', '').replace(']', '').split(',') : [];
        const counts = countsRaw ? countsRaw.replace('[', '').replace(']', '').split(',') : [];

        new Chart(cityChartCanvas, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'İlan Sayısı',
                    data: counts,
                    backgroundColor: '#f59e0b',
                    borderRadius: 5
                }]
            },
            options: {
                responsive: true,
                scales: { y: { beginAtZero: true, ticks: { stepSize: 1 } } }
            }
        });
    }

    // ==========================================
    // 8. AI CHATBOT BAŞLATMA (OTOMATİK)
    // ==========================================
    // Eğer sayfada chat widget yoksa ekle
    if (!document.getElementById('ai-chat-widget')) {
        createChatWidget();
    }
});

// ==========================================
// 9. GLOBAL FONKSİYONLAR (Window'a Bağlı)
// ==========================================

// --- A. Yapay Zeka CV Analizi ---
window.aiTavsiyeAl = function() {
    
    // 1. Yükleniyor mesajı göster (Popup olarak kalsın, kullanıcı beklediğini anlasın)
    Swal.fire({
        title: 'Analiz Yapılıyor... 🧠',
        text: 'Profilin ve CV detayların inceleniyor. Lütfen bekle...',
        allowOutsideClick: false,
        didOpen: () => { Swal.showLoading() }
    });

    // 2. Backend'e istek at
    fetch('/api/ai/cv-tavsiye')
        .then(response => response.text())
        .then(data => {
            // Loading popup'ını kapat
            Swal.close();

            // HTML içindeki kutuyu bul
            const resultBox = document.getElementById('aiResultBox');
            const contentDiv = document.getElementById('aiContent');

            if (resultBox && contentDiv) {
                // Cevaptaki satır başlarını HTML <br> etiketiyle değiştir
                // ve yıldız (*) işaretlerini madde işareti gibi göster
                let formattedData = data
                    .replace(/\n/g, '<br>')
                    .replace(/\*\*/g, ''); // Kalınlaştırma işaretlerini temizle (İstersen CSS ile bold yapabilirsin)

                // Kutunun içine yaz
                contentDiv.innerHTML = formattedData;
                
                // Kutuyu görünür yap
                resultBox.style.display = 'block';

                // Sayfayı kutuya doğru hafifçe kaydır (Scroll)
                resultBox.scrollIntoView({ behavior: 'smooth', block: 'center' });
            } 
            // Eğer kutu HTML'de yoksa (eski sayfadaysa) yine Popup aç
            else {
                Swal.fire({
                    title: '✨ Dijital Kariyer Koçun:', // İSMİ BURADAN DA DEĞİŞTİREBİLİRSİN
                    html: data.replace(/\n/g, '<br>'),
                    icon: 'info',
                    width: '600px'
                });
            }
        })
        .catch(error => {
            console.error(error);
            Swal.fire('Hata', 'Yapay zeka servisine ulaşılamadı. Lütfen tekrar dene.', 'error');
        });
};
// --- D. İşveren İçin İlan Yazdırma ---
window.aiIlanYaz = function() {
    const baslikInput = document.getElementById('ilanBaslik');
    const aciklamaInput = document.getElementById('ilanAciklama');

    if (!baslikInput || baslikInput.value.trim() === '') {
        Swal.fire('Eksik Bilgi', 'Lütfen önce bir "İlan Başlığı" yazın, yapay zeka ona göre içerik üretecek.', 'warning');
        return;
    }

    Swal.fire({
        title: 'İlan Hazırlanıyor... ✍️',
        text: `'${baslikInput.value}' pozisyonu için profesyonel bir metin yazılıyor.`,
        allowOutsideClick: false,
        didOpen: () => { Swal.showLoading() }
    });

    // Backend'e istek at
    fetch('/api/ai/ilan-olustur?baslik=' + encodeURIComponent(baslikInput.value))
        .then(response => response.text())
        .then(data => {
            Swal.close();
            
            // Gelen metni textarea'ya yaz (Var olanın üzerine eklemesin, direkt yazsın mı? Bence direkt yazsın)
            // HTML taglerini temizleyip düz metin olarak da koyabiliriz ama şimdilik direkt basalım.
            // Textarea HTML taglerini render etmez, o yüzden basit bir temizlik yapalım:
            let temizMetin = data.replace(/<br>/g, '\n').replace(/<b>/g, '').replace(/<\/b>/g, '');
            
            aciklamaInput.value = temizMetin;
            
            Swal.fire({
                icon: 'success',
                title: 'Metin Hazır!',
                text: 'İlan açıklaması otomatik dolduruldu. Üzerinde düzenleme yapabilirsin.',
                timer: 2000,
                showConfirmButton: false
            });
        })
        .catch(error => {
            console.error(error);
            Swal.fire('Hata', 'Bağlantı sorunu oluştu.', 'error');
        });
};
// --- B. Kullanıcı Silme Onayı ---
window.kullaniciSil = function(id) {
    Swal.fire({
        title: 'Kullanıcı Banlanacak!',
        text: "Bu işlem geri alınamaz.",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Evet, Banla!',
        cancelButtonText: 'Vazgeç'
    }).then((result) => {
        if (result.isConfirmed) {
            window.location.href = `/admin/kullanici-sil/${id}`;
        }
    });
};

// --- C. İlan Silme Onayı ---
window.ilanSil = function(id) {
    Swal.fire({
        title: 'İlan Kaldırılıyor',
        input: 'text',
        inputLabel: 'Silme Sebebi:',
        inputPlaceholder: 'Örn: Uygunsuz içerik...',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        confirmButtonText: 'Sil',
        inputValidator: (value) => {
            if (!value) return 'Sebep yazmalısınız!'
        }
    }).then((result) => {
        if (result.isConfirmed) {
            window.location.href = `/admin/ilan-sil/${id}?sebep=${encodeURIComponent(result.value)}`;
        }
    });
};

// --- URL Parametresi Kontrolü (Mesaj Gösterimi) ---
const urlParams = new URLSearchParams(window.location.search);
if (urlParams.has('msg')) {
    let msg = urlParams.get('msg');
    let title = 'İşlem Başarılı';
    
    if(msg === 'vitrin_basarili') title = 'İlan Vitrine Eklendi!';
    if(msg === 'paket_silindi') title = 'Paket Silindi!';
    
    Swal.fire({
        icon: 'success',
        title: title,
        timer: 1500,
        showConfirmButton: false
    });
}

// ==========================================
// 10. CHATBOT FONKSİYONLARI VE HTML
// ==========================================

function createChatWidget() {
    const chatHTML = `
        <div id="ai-chat-widget" style="position: fixed; bottom: 20px; right: 20px; z-index: 9999; font-family: 'Segoe UI', sans-serif;">
            
            <div id="ai-chat-box" style="display: none; width: 320px; height: 450px; background: white; border-radius: 15px; box-shadow: 0 5px 25px rgba(0,0,0,0.2); flex-direction: column; overflow: hidden; margin-bottom: 15px; border: 1px solid #e2e8f0;">
                
                <div style="background: linear-gradient(135deg, #6366f1, #8b5cf6); color: white; padding: 15px; font-weight: bold; display: flex; justify-content: space-between; align-items: center;">
                    <div style="display:flex; align-items:center; gap:8px;">
                        <span style="font-size: 1.2rem;">🤖</span>
                        <span>Kariyer Asistanı</span>
                    </div>
                    <span onclick="toggleChat()" style="cursor: pointer; font-size: 1.2rem; opacity: 0.8;">&times;</span>
                </div>

                <div id="ai-messages" style="flex: 1; padding: 15px; overflow-y: auto; background: #f8fafc; font-size: 0.9rem; scroll-behavior: smooth;">
                    <div style="margin-bottom: 15px; color: #64748b; text-align: center; font-size: 0.8rem;">
                        Merak ettiğin her şeyi sorabilirsin!
                    </div>
                    <div style="background: #e0e7ff; color: #3730a3; padding: 10px 14px; border-radius: 15px 15px 15px 0; margin-bottom: 10px; max-width: 85%; width: fit-content; box-shadow: 0 1px 2px rgba(0,0,0,0.05);">
                        Merhaba! 👋 Sana nasıl yardımcı olabilirim? CV hazırlama, mülakat tüyoları veya site kullanımı hakkında sorabilirsin.
                    </div>
                </div>

                <div style="padding: 12px; border-top: 1px solid #e2e8f0; display: flex; gap: 8px; background: white;">
                    <input type="text" id="ai-input" placeholder="Bir şeyler yaz..." 
                           style="flex: 1; padding: 10px; border: 1px solid #cbd5e1; border-radius: 20px; outline: none; font-size: 0.9rem; transition: border-color 0.3s;"
                           onfocus="this.style.borderColor='#6366f1'"
                           onblur="this.style.borderColor='#cbd5e1'"
                           onkeypress="if(event.key === 'Enter') sendMessage()">
                    <button onclick="sendMessage()" style="background: #6366f1; color: white; border: none; width: 40px; height: 40px; border-radius: 50%; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: transform 0.2s;">
                        ➤
                    </button>
                </div>
            </div>

            <button onclick="toggleChat()" style="width: 60px; height: 60px; border-radius: 50%; background: linear-gradient(135deg, #4f46e5, #ec4899); color: white; border: none; box-shadow: 0 4px 15px rgba(79, 70, 229, 0.4); cursor: pointer; display: flex; align-items: center; justify-content: center; font-size: 1.8rem; transition: transform 0.3s; margin-left: auto;" onmouseover="this.style.transform='scale(1.1)'" onmouseout="this.style.transform='scale(1)'">
                💬
            </button>
        </div>
    `;
    document.body.insertAdjacentHTML('beforeend', chatHTML);
}

// Chat Penceresini Aç/Kapa
window.toggleChat = function() {
    const box = document.getElementById('ai-chat-box');
    if (box.style.display === 'none' || box.style.display === '') {
        box.style.display = 'flex';
        // Kutuyu açınca inputa odaklan
        setTimeout(() => document.getElementById('ai-input').focus(), 100);
    } else {
        box.style.display = 'none';
    }
};

// Mesaj Gönderme
window.sendMessage = function() {
    const input = document.getElementById('ai-input');
    const messagesDiv = document.getElementById('ai-messages');
    const userMsg = input.value.trim();

    if (!userMsg) return;

    // 1. Kullanıcı mesajını ekrana bas
    messagesDiv.innerHTML += `
        <div style="margin-bottom: 10px; display: flex; justify-content: flex-end;">
            <div style="background: #6366f1; color: white; padding: 10px 14px; border-radius: 15px 15px 0 15px; max-width: 85%; box-shadow: 0 2px 4px rgba(99, 102, 241, 0.2);">
                ${userMsg}
            </div>
        </div>
    `;
    
    // Inputu temizle ve scroll'u aşağı indir
    input.value = '';
    messagesDiv.scrollTop = messagesDiv.scrollHeight;

    // 2. "Yazıyor..." efekti
    const loadingId = 'loading-' + Date.now();
    messagesDiv.innerHTML += `
        <div id="${loadingId}" style="margin-bottom: 10px; display: flex;">
            <div style="background: #f1f5f9; color: #64748b; padding: 8px 12px; border-radius: 15px 15px 15px 0; font-style: italic; font-size: 0.8rem; border: 1px solid #e2e8f0;">
                Yazıyor... ✍️
            </div>
        </div>
    `;
    messagesDiv.scrollTop = messagesDiv.scrollHeight;

    // 3. Sunucuya Gönder (POST İsteği - Query Param ile)
    // Controller @RequestParam beklediği için URL'ye ekliyoruz
    fetch('/api/ai/sohbet?mesaj=' + encodeURIComponent(userMsg), { method: 'POST' })
        .then(response => response.text())
        .then(aiResponse => {
            // Loading mesajını sil
            const loadingEl = document.getElementById(loadingId);
            if(loadingEl) loadingEl.remove();

            // AI Cevabını Ekle
            // Basit formatlama: Satır sonlarını <br> yap
            const formattedResponse = aiResponse.replace(/\n/g, '<br>');

            messagesDiv.innerHTML += `
                <div style="margin-bottom: 10px; display: flex;">
                    <div style="background: #e0e7ff; color: #3730a3; padding: 10px 14px; border-radius: 15px 15px 15px 0; max-width: 85%; box-shadow: 0 1px 2px rgba(0,0,0,0.05); line-height: 1.4;">
                        ${formattedResponse}
                    </div>
                </div>
            `;
            messagesDiv.scrollTop = messagesDiv.scrollHeight;
        })
        .catch(err => {
            const loadingEl = document.getElementById(loadingId);
            if(loadingEl) loadingEl.remove();
            
            messagesDiv.innerHTML += `
                <div style="margin-bottom: 10px; display: flex;">
                    <div style="background: #fee2e2; color: #b91c1c; padding: 8px 12px; border-radius: 15px; font-size: 0.8rem;">
                        ⚠️ Bağlantı hatası oluştu.
                    </div>
                </div>
            `;
        });
};