# SoukMar Android — Claude Working Guide

## Projekt
Native Android-App (Kotlin, Jetpack Compose) für SoukMar, das marokkanische Kleinanzeigen-Portal.
Spricht direkt mit dem bestehenden Backend — **kein separates Android-Backend**, gleiche API wie die Web-App.

**Pfad:** `C:\AIProjekte\soukmar-android`
**GitHub:** github.com/azizennajih/soukmar-android
**Backend:** `C:\AIProjekte\soukmar-backend` (Port 3000) — siehe dessen eigenes `CLAUDE.md`
**Web-App (Referenz für Verhalten/Copy):** `C:\AIProjekte\soukmar`

Die Android-App wird **phasenweise** mit vollem Funktionsumfang aufgebaut (Nutzerentscheidung: "Android zuerst", "voller Funktionsumfang" statt MVP). Bei jeder neuen Funktion zuerst die entsprechende Web-Implementierung lesen (Component/Service/Route) und exakt spiegeln — Feldnamen, Endpunkte, Validierungsregeln, Copy-Ton.

---

## Stack
- Kotlin + Jetpack Compose (Material 3)
- Hilt (Dependency Injection)
- Retrofit + OkHttp + kotlinx.serialization (REST)
- Socket.IO Client (Chat, Echtzeit — Dependency vorhanden, noch nicht verdrahtet)
- Coil (Bilder)
- DataStore (Session/Token, entspricht `localStorage` im Web-Frontend)
- Navigation Compose

**Package:** `com.soukmar.app` · `minSdk 26` · `compileSdk`/`targetSdk 34`

---

## Build-Toolchain (dieser Rechner)

Auf diesem Windows-Rechner mussten JDK 17, Android SDK und Gradle-Wrapper manuell installiert werden (kein Android Studio vorinstalliert). Bei einem frischen Checkout:

```
sdk.dir=C:/Android/sdk
```
in `local.properties` eintragen (gitignored, existiert nicht nach `git clone`).

| Was | Pfad |
|-----|------|
| JDK 17 | `C:\Android\jdk17\jdk-17.0.12+7` |
| Android SDK | `C:\Android\sdk` |
| Gradle (Wrapper lädt automatisch 8.7) | — |

Build: `JAVA_HOME=C:\Android\jdk17\jdk-17.0.12+7` setzen, dann `./gradlew assembleDebug`.

Debug-Build zeigt auf `http://10.0.2.2:3000/api/` (Emulator-Alias für den Host-PC). Bei einem **echten Gerät** statt Emulator die LAN-IP des Rechners in `app/build.gradle.kts` (`API_BASE_URL`) eintragen.

**Bekanntes Problem:** Der Android-Emulator läuft innerhalb der Claude-Code-Sandbox (verschachtelte Virtualisierung, Software-Rendering) teils extrem langsam — bis hin zu System-weiten ANRs (auch bei fremden System-Apps, nicht nur SoukMar). Das ist kein App-Fehler. Mildernd: niedrigere AVD-Auflösung (z.B. 720×1280 statt 1080×2340) und `hw.cpu.ncore=1` reduzieren die Last spürbar. Texteingabe per `adb shell input text` ist auf dem langsamen Emulator unzuverlässig (Zeichen gehen verloren) — zeichenweise mit kleinen Pausen (`sleep 0.3-0.5`) senden statt am Stück.

---

## Design System — IMMER mit `soukmar/src/styles.scss` synchron halten

`ui/theme/Color.kt` spiegelt die CSS-Variablen der Web-App 1:1:

| Compose-Konstante | Wert | Verwendung |
|--------------------|------|------------|
| `Primary` | `#D93D4A` | Buttons, CTAs, Logo, Hover |
| `PrimaryDark` | `#B8313C` | Hover-Zustand |
| `PrimaryLight` | `#FEF1F2` | Hintergrund für Primär-Highlights |
| `Gold` | `#C9941A` | Premium-Badges |
| `GoldLight` | `#FEF6E4` | Hintergrund für Gold-Highlights |
| `Secondary` | `#1E3A5F` | sekundäre Elemente |
| `TextPrimary` | `#111827` | Haupttext |
| `TextMuted` | `#6B7280` | Sekundärtext |
| `BorderColor` | `#E5E9EE` | Rahmen |
| `BgColor` | `#F8F7F5` | Seitenhintergrund |
| `ErrorColor` | `#DC2626` | Fehler |
| `SuccessColor` | `#16A34A` | Erfolg |

Kategorie-Badge-Farben (bg/fg-Paare) in `ui/model/CatalogModels.kt` — spiegeln `.cat-*`-Klassen aus `soukmar/src/styles.scss`.

**Keine hardcodierten Farben** außerhalb von `Color.kt` — immer die benannten Konstanten importieren.

---

## i18n — aktuell NICHT vorhanden

Die Android-App hat (im Gegensatz zur Web-App mit 6 Sprachen) noch keine i18n-Schicht. Alle Texte sind **hartcodiertes Französisch**, konsistent mit den Standard-Strings der Web-App. Eine i18n-Schicht nachzurüsten ist eine spätere, eigene Phase — bis dahin neue Strings einfach auf Französisch schreiben, im gleichen Ton wie die bestehenden.

---

## Architektur-Konventionen

- **State:** `HiltViewModel` mit `var x by mutableStateOf(...)` (Compose State), **kein** `StateFlow`/`LiveData` — durchgängiges Muster in diesem Projekt.
- **API-Ergebnisse:** `sealed class ApiResult<out T> { Success<T>(data); Error(message, unverified=false) }` in `data/repository/ApiResult.kt`-Kontext (definiert in `AuthRepository.kt`). Jedes Repository hat eine eigene private `parseError()`-Kopie (bewusst dupliziert, kein gemeinsames Basis-Repository — Konvention beibehalten).
- **DTOs:** `@Serializable data class` in `data/remote/dto/`, benannt nach Domäne (`AuthDto.kt`, `ListingDto.kt`, `CatalogDto.kt`, `ReviewDto.kt`, `ReportDto.kt`, `ChatDto.kt`). Felder nullable mit Default, wenn die Backend-Route sie weglassen kann.
- **Repositories:** `@Singleton`, injizieren `ApiService` + `Json` (für Fehler-Parsing). Ein Repository pro Domäne (Auth, Listing, Catalog, Review, Report, Chat).
- **Compose Screens:** Datei-Header `@file:OptIn(ExperimentalMaterial3Api::class)` (und `ExperimentalFoundationApi::class` bei Pager-Nutzung) statt einzelner `@OptIn` pro Funktion.
- **Navigation:** `Routes.kt` als zentrales Objekt mit Konstanten + Helper-Funktionen für parametrisierte Routen (`Routes.listingDetail(id)` statt String-Interpolation im Aufrufer).
- **Farben/Kategorien:** `ui/model/CatalogModels.kt` — statische Kopie von `CATEGORIES`/`CONDITION_CATEGORIES`/`HIGHLIGHT_ATTR_CODES` aus `listing.model.ts`. Bei Änderungen am Web-Katalog **beide Seiten** aktualisieren.
- **"Tap to open a dropdown" auf einem `readOnly` `OutlinedTextField`:** ein `.clickable{}` direkt auf dem TextField feuert unzuverlässig nicht — das TextField konsumiert den Tap selbst zuerst für seinen eigenen Fokus/Cursor. Stattdessen einen transparenten Overlay-`Box(Modifier.matchParentSize().clickable{...})` als Geschwister-Element *über* dem TextField platzieren (siehe `CityDropdown`/`AttributeField` SELECT-Case in `DeposerAnnonceScreen.kt`).

---

## Dynamisches Attribut-System (EAV) — wie im Web

Kategorien haben Unterkategorien (`Subcategory`), jede mit eigenen `AttributeDefinition`-Feldern (Text/Zahl/Auswahl/Ja-Nein, Pflicht-/Filter-Flag). Werte pro Anzeige in `ListingAttributeValue`. Katalog-Endpunkte unter `/api/catalog/*` (`CatalogRepository`). Suchfilter nutzen `attr_<CODE>` (Auswahl/Bool) bzw. `attr_<CODE>_min`/`_max` (Zahl-Bereich) — siehe `ListingsViewModel.buildParams()`.

---

## Phasenplan (voller Funktionsumfang, Android zuerst)

Reihenfolge und Stand:

1. ✅ Auth (Login/Register/Passwort vergessen+zurücksetzen)
2. ✅ Home + Anzeigen durchsuchen/suchen mit dynamischen EAV-Filtern
3. ✅ Anzeigendetail (Galerie, Attribute, Preis-Indikator, Favoriten, Bewertung, Melden, Chat-Einstieg)
4. ✅ Anzeige aufgeben (Assistent mit dynamischen Attributen, Foto-Upload, Premium/Telefon-Toggle) — `ui/screens/deposerannonce/`. ViewModel unterstützt bereits Edit-Modus (optionaler `id`-Routenparameter, `ListingUpsertRequest` für POST **und** PUT), aber im NavGraph ist aktuell nur der Create-Einstieg (FAB auf Home) verdrahtet — der Edit-Einstieg kommt mit Phase 6 (Mes Annonces → "Bearbeiten"-Button navigiert zu `Routes.deposerAnnonce(listing.id)`). Foto-Picker nutzt `ActivityResultContracts.GetMultipleContents()` (kein Photo-Picker-Play-Services-Zwang). Reihenfolge/Hauptbild wird per Tap-to-promote gelöst (kein Drag&Drop wie im Web).
5. ✅ Chat (Socket.IO Echtzeit, Angebote, Schnellantworten, Melden) — `ui/screens/chat/` (Liste + Konversation), `data/remote/ChatSocketManager.kt`. Gleiches Event-Protokoll wie `soukmar-backend/src/socket.ts` (`send_message`, `send_offer`, `respond_offer`, `cancel_offer`, `cancel_reservation`, `typing` → `new_message`, `offer_updated`, `user_typing`, `listing_status_changed`). Es gibt **kein** eigenes REST-Endpoint zum Senden — Nachrichten/Angebote laufen ausschließlich über den Socket. Einstieg über Chat-Icon in `HomeScreen`-TopAppBar sowie über "Contacter le vendeur" in `ListingDetailScreen` (navigiert jetzt direkt in die Konversation statt der alten Platzhalter-Snackbar).
6. ✅ Meine Anzeigen (Bump, 14-Tage-Statistik) — `ui/screens/mesannonces/`. Liste eigener Anzeigen mit Status-Badge, Aktionen (Voir/Réserver-Toggle/Modifier/Bump/Statistiken/Supprimer). "Modifier" nutzt den bereits in Phase 4 vorbereiteten Edit-Modus von `DeposerAnnonceScreen` (`Routes.deposerAnnonce(id)`) — der Nav-Eintrag dafür fehlte bisher und ist jetzt verdrahtet. Einstieg über Icon in `HomeScreen`-TopAppBar.
7. ⬜ Favoriten-Übersicht
8. ⬜ Profil (bearbeiten + Passwort ändern)
9. ⬜ Verkäuferprofil (Bewertungen, Antwortzeit)
10. ⬜ Gespeicherte Suchen
11. ⬜ Benachrichtigungen + Push (FCM)
12. ⬜ Admin (Meldungen-Tab wie im Web)
13. ⬜ i18n-Schicht (6 Sprachen wie Web)
14. ⬜ iOS-App (später, braucht Mac oder Cloud-CI wie Codemagic)

**Jede Phase muss getestet werden**, bevor die nächste beginnt (Nutzer-Vorgabe) — Claude übernimmt das Testen selbst (Build + Emulator/adb), nicht der Nutzer manuell. Siehe Abschnitt "Testen" unten.

---

## Testen

Reihenfolge pro Phase:
1. `./gradlew assembleDebug` — muss sauber durchlaufen (`BUILD SUCCESSFUL`), keine neuen Fehler.
2. Backend-Dev-Server muss auf Port 3000 laufen (`soukmar-backend`, `npm run dev` o.ä.).
3. Emulator starten (AVD `soukmar_test`, falls vorhanden, sonst neu anlegen — niedrige Auflösung wählen, siehe oben) und die neue/geänderte Funktion tatsächlich durchklicken (adb screencap + adb input, siehe "Bekanntes Problem" oben für Geschwindigkeits-Workarounds).
4. Demo-Accounts: `ahmed@soukmar.ma` / `fatima@soukmar.ma` / `youssef@soukmar.ma`, Passwort jeweils `soukmar123`.
5. Erst nach erfolgreichem Test committen + zu `github.com/azizennajih/soukmar-android` pushen.

**Achtung Workflow-Datei:** `.github/workflows/android-build.yml` kann mit dem aktuellen GitHub-Token nicht gepusht werden (fehlender `workflow`-Scope) — bleibt lokal im Arbeitsverzeichnis, nicht versioniert. Nicht erneut versuchen zu pushen, bis der Nutzer den Token-Scope erweitert hat.

---

## Wichtige Dateipfade

| Was | Pfad |
|-----|------|
| API-Interface | `app/src/main/java/com/soukmar/app/data/remote/ApiService.kt` |
| DTOs | `app/src/main/java/com/soukmar/app/data/remote/dto/` |
| Repositories | `app/src/main/java/com/soukmar/app/data/repository/` |
| Token/Session | `app/src/main/java/com/soukmar/app/data/local/TokenManager.kt` |
| Farben/Theme | `app/src/main/java/com/soukmar/app/ui/theme/` |
| Katalog-Modelle (Kategorien etc.) | `app/src/main/java/com/soukmar/app/ui/model/CatalogModels.kt` |
| Wiederverwendbare Composables | `app/src/main/java/com/soukmar/app/ui/components/` |
| Screens | `app/src/main/java/com/soukmar/app/ui/screens/<feature>/` |
| Navigation | `app/src/main/java/com/soukmar/app/ui/navigation/` |
