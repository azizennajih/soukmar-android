# SoukMar — Android (nativ)

Native Android-App für SoukMar (Kotlin, Jetpack Compose), spricht direkt mit dem
bestehenden Backend unter [`soukmar-backend`](https://github.com/azizennajih/soukmar-backend).

## Stack
- Kotlin + Jetpack Compose (Material 3)
- Hilt (Dependency Injection)
- Retrofit + OkHttp + kotlinx.serialization (REST)
- Socket.IO Client (Chat, Echtzeit)
- Coil (Bilder)
- DataStore (Session/Token, entspricht `localStorage` im Web-Frontend)

## Setup
1. Android SDK (compileSdk 34, minSdk 26) + JDK 17 installiert
2. `local.properties` mit `sdk.dir=<Pfad zum Android SDK>` anlegen (nicht versioniert)
3. `./gradlew assembleDebug`

Der Debug-Build zeigt auf `http://10.0.2.2:3000/api/` — das ist die Emulator-Alias-Adresse
für den lokalen Backend-Dev-Server (`soukmar-backend`, Port 3000) auf dem Host-Rechner.
Für ein echtes Gerät im selben WLAN die IP des Rechners in `app/build.gradle.kts`
(`API_BASE_URL`) anpassen.

## Stand
Grundgerüst + kompletter Auth-Flow (Login, Registrierung, Passwort vergessen/
zurücksetzen) gegen das echte Backend. Weitere Bereiche (Anzeigen durchsuchen,
Detailseite, Anzeige aufgeben, Chat, Profil, Favoriten, Bewertungen, Meldungen,
Push) folgen schrittweise in den nächsten Phasen.
