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
- osmdroid (Karten — OpenStreetMap-Kacheln, spiegelt Webs Leaflet/OSM; kein API-Key nötig, im Gegensatz zu Google Maps)

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

**Firebase/FCM-Setup (Phase 11, noch offen):** `app/google-services.json` ist ein **Platzhalter mit Fake-Werten** (nur strukturell valide, damit `com.google.gms.google-services` den Build nicht bricht) — für echten Push muss ein reales Firebase-Projekt angelegt und die echte `google-services.json` hier eingespielt werden, **plus** im `soukmar-backend`-Repo die Env-Var `FIREBASE_SERVICE_ACCOUNT` (JSON des Service-Account-Keys als String) gesetzt werden. Ohne das no-opt der Push-Versand serverseitig lautlos (kein Fehler, kein Crash) — die In-App-Benachrichtigungsliste funktioniert unabhängig davon bereits vollständig.

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

## i18n — seit Phase 13 vorhanden (6 Sprachen wie Web)

Neue UI-Texte **immer** über `t("namespace.key")` (aus `com.soukmar.app.ui.i18n`) schreiben, nie mehr hartcodiertes Französisch. Die Übersetzungsdateien sind `app/src/main/assets/i18n/{fr,en,ar,de,es,it}.json` — **1:1-Kopien der Web-Dateien** (`soukmar/src/assets/i18n/`), nicht manuell pflegen. Ein neuer Key muss zuerst im Web-Repo existieren (oder dort mit ergänzt werden) und dann per Datei-Kopie hier landen — nicht eigenständig in der Android-JSON erfinden, sonst laufen beide Apps auseinander. Für Backend-getriebene Katalog-Codes (Kategorie/Subkategorie/EAV-Attribut/-Option) `tCatalog("cats.$code", code)` etc. verwenden (Fallback auf `humanizeCode()`). Details, Architektur und bekannte Lücken siehe Phase-13-Eintrag im Phasenplan unten.

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

## Phasenplan / Verlauf

Alle Phasen (1–13) und Tranchen 1–21 sind abgeschlossen. Das ausführliche Protokoll (je Eintrag: Was, Warum, Fallstricke, Testergebnis) steht in `docs/phasen-verlauf.md`. **Nicht komplett lesen**, nur gezielt nach dem passenden Stichwort suchen (Grep) — es ist sehr lang.

Bei einer neuen Tranche: Eintrag **unten an `docs/phasen-verlauf.md` anhängen** (kurz: Datum, Thema, Dateien, Fallstricke, was getestet/nicht getestet ist) — nicht in diese Datei.

---

## Testen

Reihenfolge pro Phase:
1. `./gradlew assembleDebug` — muss sauber durchlaufen (`BUILD SUCCESSFUL`), keine neuen Fehler.
2. Backend-Dev-Server muss auf Port 3000 laufen (`soukmar-backend`, `npm run dev` o.ä.).
3. Emulator starten (AVD `soukmar_test`, falls vorhanden, sonst neu anlegen — niedrige Auflösung wählen, siehe oben) und die neue/geänderte Funktion tatsächlich durchklicken (adb screencap + adb input, siehe "Bekanntes Problem" oben für Geschwindigkeits-Workarounds).
4. Demo-Accounts: `ahmed@soukmar.ma` / `fatima@soukmar.ma` / `youssef@soukmar.ma`, Passwort jeweils `soukmar123`.
5. Erst nach erfolgreichem Test committen + zu `github.com/azizennajih/soukmar-android` pushen.

**Installierbarer Test-Build für ein echtes Handy/Tablet (seit 2026-10-15):** `./gradlew assembleLive` (JAVA_HOME wie oben, ca. 6 Min) -> `app/build/outputs/apk/live/app-live.apk`. Spricht mit https://souqmar24.com (wie Release), ist mit dem Debug-Schlüssel signiert und lässt sich per Sideload installieren (Quellen: unbekannte Apps erlauben). Der Debug-Build (`assembleDebug`) spricht dagegen nur mit 10.0.2.2 (Emulator). Für den Play Store braucht es später einen eigenen Release-Schlüssel.

**Achtung Workflow-Datei:** `.github/workflows/android-build.yml` kann mit dem aktuellen GitHub-Token nicht gepusht werden (fehlender `workflow`-Scope) — bleibt lokal im Arbeitsverzeichnis, nicht versioniert. Nicht erneut versuchen zu pushen, bis der Nutzer den Token-Scope erweitert hat.

---

## Arbeitsweise (Tokens sparen)
- Große Dateien **nie komplett lesen**: die i18n-JSON-Dateien (je >1500 Zeilen), `package-lock.json`, `docs/phasen-verlauf.md`, Build-Logs. Stattdessen Grep nach dem Schlüssel/Stichwort oder `Read` mit `offset`/`limit`.
- Erst gezielt suchen (Grep/Glob), dann nur die betroffene Stelle lesen; nicht ganze Ordner durchlesen.
- Lange Befehlsausgaben filtern (`Select-String`, `| Select-Object -Last 20`), besonders bei Gradle, npm und Tests.
- Pro Aufgabe eine **neue Sitzung** (`/clear`): ein langer Verlauf wird bei jeder Nachricht mitgetragen. Dauerhaftes Wissen gehört in diese Datei oder ins Memory, nicht in den Chatverlauf.

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
