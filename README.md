# StramingCommunityClient — GeckoView single-site viewer per Android TV

<p align="center">
  <img src="app/src/main/res/drawable-nodpi/launcher_icon.png" width="180" alt="StramingCommunityClient launcher icon">
</p>

StramingCommunityClient è un esperimento Android TV minimale scritto in Kotlin. Incorpora GeckoView nell'APK e apre automaticamente un singolo URL a schermo intero, senza dipendere da Android System WebView o dal browser preinstallato sul dispositivo.

Non è un browser completo: è un single-site viewer pensato principalmente per verificare GeckoView su hardware Android TV vecchio o limitato.

## Stato attuale

- GeckoView `153.0.20260810162159`.
- Kotlin e Android Views/XML; nessun Jetpack Compose.
- `minSdk 29`, `targetSdk 37`, `compileSdk 37`.
- Java source/target compatibility 17.
- Gradle 9.5.0 e Android Gradle Plugin 9.3.1.
- Orientamento landscape.
- Un'unica `MainActivity` e un unico `GeckoView`.
- URL salvato localmente tramite `SharedPreferences`.
- Nessun WebView di sistema utilizzato.

## Hardware e ABI verificati

| Flavor | ABI | Ambiente | Stato |
|---|---|---|---|
| `hboxArmv7` | `armeabi-v7a` | H_BOX, Android 10/API 29, dispositivo low-RAM | Verificato |
| `emulatorX86_64` | `x86_64` | Emulatore Android TV API 36 | Verificato |
| `emulatorX86` | `x86` | Vecchio AVD API 29 | Flavor storico; GeckoView 153 non pubblica x86 |

Gli APK sono ABI-specifici. Non viene prodotto un APK universale.

## Configurazione dell'URL dalla TV

L'app carica l'URL salvato; se non esiste ancora una configurazione usa il valore `DEFAULT_URL` presente in `MainActivity.kt`.

Per cambiarlo:

1. premere contemporaneamente `DPAD_UP` e `DPAD_DOWN`;
2. sui telecomandi che non possono premere due direzioni insieme, premere rapidamente `SU` e `GIÙ` entro circa 350 ms;
3. modificare l'indirizzo nel campo centrale;
4. premere `OK` o `Invio` per salvare e caricare il nuovo sito;
5. premere `BACK` per annullare.

Se viene inserito soltanto un dominio, l'app aggiunge automaticamente `https://`. Sono accettati esclusivamente URL HTTP e HTTPS validi.

## Build

Requisiti:

- JDK 17;
- Android SDK con API 37;
- accesso al repository Maven ufficiale Mozilla già configurato in `settings.gradle.kts`.

Build H_BOX ARMv7 su Windows:

```cmd
gradlew.bat :app:assembleHboxArmv7Debug
```

Build emulatore x86_64:

```cmd
gradlew.bat :app:assembleEmulatorX86_64Debug
```

Non utilizzare il flavor `emulatorX86` con GeckoView 153. Per il vecchio x86 era stata verificata GeckoView `144.0.20251027123126`.

Installazione tramite ADB:

```cmd
adb -s <seriale> install -r <percorso-apk>
```

Usare sempre `-s <seriale>` quando sono connessi più dispositivi.

## Punti di forza

- Motore Gecko moderno incorporato direttamente nell'APK.
- Indipendenza dal WebView/browser obsoleto del firmware.
- Compatibilità verificata con Android 10/API 29 e ARMv7.
- Supporto x86_64 per un emulatore Android TV moderno.
- Interfaccia estremamente minimale, senza toolbar o componenti browser visibili.
- URL modificabile dalla TV e persistente tra gli avvii.
- Icona e banner TV dedicati.
- Nessuna iniezione JavaScript, WebExtension o navigazione spaziale attiva.
- Nessun servizio in background aggiunto dall'app.

## Limitazioni e debolezze note

- **Non è un browser completo.** Non dispone di barra URL permanente, tab, ricerca, cronologia visibile, preferiti, download, menu o impostazioni.
- **Nessuna navigazione Back/Forward del browser.** Non sono implementati comandi `goBack()` o `goForward()`.
- **Navigazione TV non implementata.** Il sito non viene adattato alla navigazione spaziale con D-pad.
- **Serve spesso un mouse o air-mouse.** Su H_BOX molti elementi del sito richiedono un mouse USB/Bluetooth, un telecomando air-mouse o un altro sistema di puntamento.
- **Il D-pad funziona soltanto dove la pagina offre elementi HTML naturalmente focalizzabili.**
- **Possibile lag su hardware low-RAM.** La H_BOX testata dispone di circa 1,5 GB di RAM ed è identificata da Android come dispositivo `low_ram=true`.
- **Le pagine web pesanti possono rallentare tutta la UI.** Script, pubblicità, tracker, iframe e processi Content multipli possono produrre jank, anche se la decodifica di un MP4 diretto è fluida.
- **Audio e video possono presentare scatti su siti pesanti.** Durante i test il problema ha coinvolto anche animazioni e scrolling della pagina, non soltanto il decoder video.
- **APK grande.** L'APK ARMv7 debug con GeckoView incorporato è di circa 171 MB.
- **Nessun content blocker o ad blocker.** Tracker, pubblicità e iframe vengono caricati normalmente.
- **Nessun popup blocker.** La baseline attuale non filtra finestre, redirect o richieste esterne della pagina.
- **Nessun custom player.** Riproduzione, controlli, fullscreen e compatibilità dipendono dal sito e da GeckoView.
- **Fullscreen HTML5 non personalizzato.** Non esiste ancora un gestore Android dedicato per tutte le richieste fullscreen dei siti.
- **Nessun ripristino avanzato della sessione.** L'app conserva soltanto l'URL configurato, non tab o cronologia.
- **Dominio predefinito fragile.** Se il sito cambia dominio o struttura, `DEFAULT_URL` deve essere aggiornato manualmente o tramite il campo nascosto.
- **Nessun hardening da produzione.** Mancano gestione errori completa, crash recovery, CI, test specifici TV, firma release e distribuzione Play Store.
- **Compatibilità limitata alle ABI dichiarate.** GeckoView 153 non supporta il vecchio x86 usato dal primo AVD.
- **Prestazioni influenzate dal sistema.** Device Mirroring, scansioni ADB frequenti, poca memoria libera e processi in background possono aumentare il lag.

## Struttura essenziale

```text
app/src/main/java/com/angel/stramingcommunityclient/MainActivity.kt
app/src/main/res/layout/activity_main.xml
app/src/main/AndroidManifest.xml
app/src/main/res/drawable-nodpi/launcher_icon.png
app/src/main/res/drawable-nodpi/launcher_banner.png
```

## Note su privacy e sicurezza

L'app salva localmente soltanto l'URL configurato. Cookie, storage web e traffico di rete sono gestiti da GeckoView e dai siti visitati. La baseline non blocca tracker, popup o redirect: non deve essere considerata un browser rinforzato o uno strumento di navigazione sicura general-purpose.

Utilizzare il progetto esclusivamente con siti e contenuti per i quali si dispone dell'autorizzazione necessaria.

## Licenza

Questo snapshot non include ancora una licenza del progetto. Prima di accettare contributi o riutilizzi pubblici, aggiungere una licenza appropriata. GeckoView e le altre dipendenze mantengono le rispettive licenze originali.
