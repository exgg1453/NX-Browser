# NX Browser

Chromium tabanlı (Android WebView / Blink) mobil tarayıcı. NX Team.

## Durum

Faz 1 tamamlandı: tarayıcı çekirdeği, özel yeni sekme sayfası, sekme yönetimi, sekme grupları, gizli mod, geçmiş ve veri temizleme, yer imleri, ayarlar.

Faz 2 planı: eklenti motoru (manifest okuma, içerik betiği enjeksiyonu, istek engelleme, chrome.* köprüsü).

## Yapı

- `browser/` sekme modeli, sekme yöneticisi, WebView fabrikası ve istemcileri
- `data/` Room veritabanı, DataStore ayarları, arama motorları
- `privacy/` veri temizleme
- `ui/` Compose arayüz
- `util/` URL ve user agent yardımcıları

## Derleme

```
gradle assembleDebug
gradle assembleRelease
```

GitHub Actions her push'ta debug ve release APK üretir.
