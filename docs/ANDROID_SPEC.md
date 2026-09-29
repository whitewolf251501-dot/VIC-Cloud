# VIC Android Assistant — Technical Specification

## 1. Native Architecture
- **Language / SDK:** Kotlin, Android SDK API 34 (Target), Min SDK 26 (Android 8.0 Oreo).
- **Core Role:** Default Digital Assistant replacing Google / Gemini Assistant on the host device.

## 2. Default Assistant Integration (`ACTION_ASSIST`)
To act as the system-wide default digital assistant:
1. `VoiceInteractionService` and `VoiceInteractionSessionService` declared in `AndroidManifest.xml` with `android.permission.BIND_VOICE_INTERACTION`.
2. Metadata pointing to `interaction_service.xml` specifying assistant capabilities.
3. Activity handling `android.intent.action.ASSIST` and `android.intent.action.VOICE_ASSIST`.
4. When user configures VIC as default in `Settings -> Apps -> Default apps -> Digital assistant app`, long-pressing Home / power button or swipe gesture immediately triggers VIC.

## 3. Redmi Note 12 Pro+ Hardware Shortcut Analysis
- **Target Device:** Redmi Note 12 Pro+ (MIUI / HyperOS).
- **Double-tap Fingerprint Sensor:**
  - MIUI/HyperOS provides Settings -> Additional Settings -> Gesture Shortcuts -> Double tap fingerprint sensor.
  - Standard MIUI options typically include: "Launch Google Assistant", "Take a screenshot", "Turn on flashlight", "Open Control center", "Open Calculator", "Silent mode".
  - **Verification Finding:** If MIUI restricts the shortcut to "Google Assistant", configuring VIC as the **Default Digital Assistant app** routes that action directly to VIC. If MIUI has a hardcoded package check for `com.google.android.googlequicksearchbox`, VIC provides an accessibility/gesture service fallback option.

## 4. Floating 3D Wolf Companion Overlay
- Utilizes Android `SYSTEM_ALERT_WINDOW` permission (Draw over other apps).
- User can toggle the floating companion ON or OFF at will.
- Renders the 1.09 MB `furry_cartoon_wolf_dressed_in_black_leather_jack.glb` model with transparent background using Filament / Google SceneView.
- Touch-draggable across the screen; tap to trigger voice listening mode; double-tap to open VIC mobile control sheet.

## 5. Multilingual Voice & Speech Pipeline
- Supports English, Hindi, and Gujarati via Android `SpeechRecognizer` with language locale fallback (`en-IN`, `hi-IN`, `gu-IN`), or cloud Whisper STT.
- Natural response playback using Android TTS engine with Indian English/Hindi voice packs.

## 6. Phone Actions & Safeguards
- **Alarms & Timers:** Native `AlarmManager` and `AlarmClock` intents (`ACTION_SET_ALARM`, `ACTION_SET_TIMER`).
- **Reminders:** Android Calendar Provider / local database notification alarms.
- **WhatsApp Messaging:** Formats WhatsApp URI / Intent (`Intent.ACTION_SEND` with `com.whatsapp`). **Strict Safeguard:** Populates message draft and prompts user review before sending; does NOT silently send without user visual confirmation.
- **Instagram:** Uses Android intent to open profile/post in official app (`instagram://user?username=...`); strictly respects platform safeguards and never bypasses authentication.
