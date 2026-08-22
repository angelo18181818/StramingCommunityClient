# StramingCommunityClient — GeckoView single-site viewer for Android TV

<p align="center">
  <img src="app/src/main/res/drawable-nodpi/launcher_icon.png" width="180" alt="StramingCommunityClient launcher icon">
</p>

StramingCommunityClient is a minimal Android TV experiment written in Kotlin. It embeds GeckoView directly in the APK and automatically opens a single configured URL in a fullscreen view, without relying on Android System WebView or the browser bundled with the device firmware.

This is not a complete web browser. It is a single-site viewer primarily intended to test modern GeckoView builds on old or resource-constrained Android TV hardware.

## Current status

- GeckoView `153.0.20260810162159`.
- Kotlin with Android Views/XML; no Jetpack Compose.
- `minSdk 29`, `targetSdk 37`, and `compileSdk 37`.
- Java source/target compatibility 17.
- Gradle 9.5.0 and Android Gradle Plugin 9.3.1.
- Landscape orientation.
- One `MainActivity` and one `GeckoView`.
- The configured URL is stored locally with `SharedPreferences`.
- Android System WebView is not used.

## Tested hardware and ABIs

| Flavor | ABI | Environment | Status |
|---|---|---|---|
| `hboxArmv7` | `armeabi-v7a` | H_BOX, Android 10/API 29, low-RAM device | Tested |
| `emulatorX86_64` | `x86_64` | Android TV API 36 emulator | Tested |
| `emulatorX86` | `x86` | Legacy API 29 AVD | Historical flavor; GeckoView 153 does not publish x86 builds |

The generated APKs are ABI-specific. The project does not produce a universal APK.

## Changing the URL from the TV

The application loads the stored URL. If no URL has been saved yet, it uses the `DEFAULT_URL` value declared in `MainActivity.kt`.

To change it:

1. press `DPAD_UP` and `DPAD_DOWN` at the same time;
2. on remotes that cannot physically press both directions together, press `UP` and `DOWN` rapidly within approximately 350 ms;
3. edit the address in the centered text field;
4. press `OK` or `Enter` to save and load the new site;
5. press `BACK` to cancel.

If only a domain name is entered, the application automatically prepends `https://`. Only valid HTTP and HTTPS URLs are accepted.

## Building

Requirements:

- JDK 17;
- Android SDK with API 37;
- access to the official Mozilla Maven repository, already configured in `settings.gradle.kts`.

Build the ARMv7 APK for H_BOX on Windows:

```cmd
gradlew.bat :app:assembleHboxArmv7Debug
```

Build the x86_64 emulator APK:

```cmd
gradlew.bat :app:assembleEmulatorX86_64Debug
```

Do not use the `emulatorX86` flavor with GeckoView 153. GeckoView `144.0.20251027123126` was the last version verified with the legacy x86 AVD used by this project.

Install an APK through ADB:

```cmd
adb -s <device-serial> install -r <apk-path>
```

Always specify `-s <device-serial>` when multiple devices are connected.

## Strengths

- A modern Gecko engine is embedded directly in the APK.
- It does not depend on an outdated firmware WebView or browser.
- Android 10/API 29 and ARMv7 compatibility has been tested on real hardware.
- An x86_64 flavor is available for a modern Android TV emulator.
- Extremely minimal visible UI, with no browser toolbar.
- The target URL can be changed from the TV and persists across launches.
- Dedicated Android TV icon and banner.
- No active JavaScript injection, WebExtension, or custom spatial-navigation layer.
- The application adds no background service.

## Known limitations and weaknesses

- **This is not a complete browser.** There is no permanent URL bar, tabs, search, visible history, bookmarks, download manager, menu, or settings screen.
- **There is no browser Back/Forward navigation.** The application does not implement `goBack()` or `goForward()`.
- **TV spatial navigation is not implemented.** The page DOM is not adapted for directional D-pad navigation.
- **A mouse or air-mouse is often required.** On H_BOX, many page elements require a USB/Bluetooth mouse, an air-mouse remote, or another pointing device.
- **The D-pad only works on elements that the page exposes as naturally focusable HTML controls.**
- **Performance can be poor on low-RAM hardware.** The tested H_BOX has approximately 1.5 GB of RAM and Android reports `low_ram=true`.
- **Heavy web pages can slow down the entire UI.** Scripts, advertisements, trackers, iframes, and multiple Gecko Content processes may produce visible jank even when direct MP4 decoding is smooth.
- **Audio and video may stutter on complex sites.** During testing, the same timing issue also affected page animations and scrolling, not only video decoding.
- **The APK is large.** The ARMv7 debug APK with embedded GeckoView is approximately 171 MB.
- **There is no content blocker or ad blocker.** Advertisements, trackers, and third-party frames load normally.
- **There is no popup blocker.** The current baseline does not filter new windows, redirects, or external navigation requests.
- **There is no custom media player.** Playback, controls, fullscreen behavior, and compatibility depend on the website and GeckoView.
- **HTML5 fullscreen is not customized.** The application does not yet provide a dedicated Android handler for every website fullscreen request.
- **There is no advanced session recovery.** Only the configured URL is retained; tabs and navigation history are not restored.
- **The default domain is fragile.** If the website changes its domain or structure, `DEFAULT_URL` must be updated in the source or through the hidden URL field.
- **The application is not production-hardened.** Complete error handling, crash recovery, CI, TV-specific automated tests, release signing, and Play Store distribution are not implemented.
- **ABI compatibility is intentionally limited.** GeckoView 153 does not support the legacy x86 architecture used by the first AVD.
- **System conditions affect performance.** Device mirroring, frequent ADB monitoring, low free storage, thermal throttling, and background processes can increase jank.

## Essential project structure

```text
app/src/main/java/com/angel/stramingcommunityclient/MainActivity.kt
app/src/main/res/layout/activity_main.xml
app/src/main/AndroidManifest.xml
app/src/main/res/drawable-nodpi/launcher_icon.png
app/src/main/res/drawable-nodpi/launcher_banner.png
```

## Privacy and security notes

The application stores only the configured URL in its own local preferences. Cookies, website storage, and network traffic are handled by GeckoView and by the visited websites. The current baseline does not block trackers, popups, or redirects and must not be treated as a hardened or general-purpose secure browser.

Use this project only with websites and content for which you have the necessary authorization.

## License

This snapshot does not currently include a project license. Add an appropriate license before accepting contributions or granting reuse rights. GeckoView and all other dependencies retain their respective original licenses.
