# 🎮 Tabu - J2ME Oyunu v22.01

Türkçe/İngilizce Tabu kelime oyunu. Nokia ve eski Java telefonları için geliştirilmiştir.

---

## 📱 Uyumluluk

- **Platform:** J2ME (MIDP-2.0 / CLDC-1.1)
- **Test Edildi:** Nokia 6303i, J2ME Loader (Android)
- **JAR Boyutu:** ~200KB

---

## 🎯 Oyun Nasıl Oynanır?

1. **Oyuna Başla** → Kart destesi seç → Hazır ekranı → **5** ile turu başlat
2. Anlatan kişi kartı açıklar, takım tahmin eder
3. **5** = Doğru (+1 puan) | **1** = Tabu (-3 puan) | **3** = Pas
4. Süre bitince tur biter, sıra değişir
5. En çok puan toplayan takım kazanır

---

## 🃏 Kart Kategorileri

### Türkçe
| Kategori | Açıklama |
|----------|----------|
| Mix | Klasik karma kartlar (800 kart) |
| Kış | Kar, buz, kış temalı kelimeler |
| Bahar | Çiçek, doğa, bahar temalı |
| Tatil | Seyahat, plaj, tatil temalı |
| Gece Yarısı | Gece hayatı, parti temalı |
| Ünlüler | Tanınmış isimler |
| Genel Kültür | Bilim, sanat, tarih |

### İngilizce
| Category | Description |
|----------|-------------|
| Mix | Classic mixed cards |
| Winter | Snow, ice, winter themed |
| Spring | Flowers, nature, spring |
| Holiday | Travel, beach, vacation |
| Midnight | Nightlife, party themed |
| Celebrities | Famous people |
| General Knowledge | Science, art, history |

---

## 🎮 Tuş Kontrolleri

| Tuş | Oyun | Menü |
|-----|------|------|
| **5 / Orta** | ✅ Doğru (+1) | Seç / Gir |
| **1 / Sol** | ❌ Tabu (-3) | — |
| **3 / Sağ** | ⏭ Pas | — |
| **2 / Yukarı** | — | Yukarı |
| **8 / Aşağı** | — | Aşağı |
| **\*** | Dur / Geri | Geri |
| **# (Sağ Üst)** | — | — |

---

## ⚙️ Ayarlar

| Ayar | Seçenekler |
|------|------------|
| Takım | 2-6 takım |
| Süre | 30 / 45 / 60 / 90 / 120 sn |
| Pas | 0 / 1 / 2 / 3 / 5 / Sınırsız |
| Tur | 1 / 2 / 3 / 4 / 5 / Sınırsız |
| Dil | Türkçe / İngilizce |
| Tema | 16 farklı tema |
| Ses | Açık / Kapalı |
| Titreşim | Açık / Kapalı |
| Müzik | Açık / Kapalı |

---

## 🎨 Temalar (16 Adet)

| # | TR | EN |
|---|----|----|
| 1 | Koyu | Dark |
| 2 | Pembe K. | Pink Dark |
| 3 | Pembe A. | Pink Light |
| 4 | Mavi | Blue |
| 5 | Turuncu | Orange |
| 6 | Orman | Forest |
| 7 | Mor | Purple |
| 8 | Sarı | Yellow |
| 9 | Buz | Ice |
| 10 | Alev | Flame |
| 11 | Okyanus | Ocean |
| 12 | Altın | Gold |
| 13 | Neon | Neon |
| 14 | Galaksi | Galaxy |
| 15 | Günbatımı | Sunset |
| 16 | Türkiye | Turkey |

---

## 🐛 Debug Modu

Ana menüde D-Pad ile **Konami kodu** gir:

```
↑ ↑ ↓ ↓ ← → ← →
```

Sonra **#** tuşuna bas → Tüm emojiler görüntülenir (263 emoji).

---

## 📊 İstatistikler

- Toplam doğru / tabu / pas sayısı
- En yüksek skor ve rekor takım
- Son 5 oyun geçmişi
- **#** ile sıfırlama

---

## 🔧 Derleme (Termux)

```bash
# Derleme
javac -source 8 -target 8 -classpath midp.jar -d build/classes \
  src/tabu/TabuData.java src/tabu/EngData.java \
  src/tabu/EmojiData.java src/tabu/EmojiDataEN.java \
  src/tabu/GameCanvas.java src/tabu/TabuMIDlet.java

# JAR oluşturma
jar cfm TurkceTabu.jar META-INF/MANIFEST.MF -C build/classes . logo.png tr.png en.png music.mid e/
java -jar proguard-7.3.2/lib/proguard.jar @proguard.pro
jar xf TurkceTabu_verified.jar
for f in tabu/*.class; do
  printf '\xca\xfe\xba\xbe\x00\x00\x00\x2e' | dd of=$f bs=1 count=8 conv=notrunc 2>/dev/null
done
jar cfm TurkceTabu_final.jar META-INF/MANIFEST.MF tabu/*.class logo.png tr.png en.png music.mid e/
```

---

## 📁 Dosya Yapısı

```
├── src/tabu/
│   ├── TabuMIDlet.java      # Ana MIDlet
│   ├── GameCanvas.java      # Oyun motoru (tüm ekranlar)
│   ├── TabuData.java        # TR kart verisi (1170 kart)
│   ├── EngData.java         # EN kart verisi (1066 kart)
│   ├── EmojiData.java       # TR emoji eşleştirme
│   └── EmojiDataEN.java     # EN emoji eşleştirme
├── e/                       # 283 emoji sprite (8x8 PNG)
├── META-INF/MANIFEST.MF
├── logo.png
├── tr.png / en.png          # Bayrak görselleri
├── music.mid                # Arka plan müziği
└── proguard.pro
```

---

## ⚠️ Teknik Notlar

- `StringBuilder` / `StringBuffer` **KULLANILAMAZ** (CLDC uyumsuz)
- `Math.random()` yerine `new Random()` kullanılır
- Tüm string birleştirmeleri `cat()` metodu ile yapılır
- Emoji büyütme piksel bazlı (J2ME'de `drawRegion` yok)
- Sınıf versiyonu: Java 8 → bytecode `0x2E` (Nokia uyumlu)

---

## 👤 Geliştirici

**UmutK** — 2026  
Lisans: MIT
