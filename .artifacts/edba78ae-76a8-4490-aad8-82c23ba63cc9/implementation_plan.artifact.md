# Complete Translator App Implementation Plan

The goal is to complete the translation application by implementing the ViewModels, the UI screens (Main and Settings), and wiring everything together in `MainActivity`.

## User Review Required

> [!IMPORTANT]
> The app uses ML Kit for on-device translation. Language models must be downloaded before they can be used. I will implement a settings screen to manage these downloads.
>
> [!NOTE]
> Speech-to-text requires the `RECORD_AUDIO` permission. I will include permission handling logic.

## Proposed Changes

### [Component] ViewModels

#### [NEW] [TranslationViewModel.kt](file:///C:/Users/Sak/AndroidStudioProjects/Traductor/app/src/main/java/com/sakatoon/traductor/viewmodel/TranslationViewModel.kt)
Handles the core translation logic, speech-to-text integration, and text-to-speech triggers. It will coordinate between `TranslationRepository`, `SpeechToTextManager`, and `TextToSpeechManager`.

#### [NEW] [SettingsViewModel.kt](file:///C:/Users/Sak/AndroidStudioProjects/Traductor/app/src/main/java/com/sakatoon/traductor/viewmodel/SettingsViewModel.kt)
Manages the list of available and downloaded language models using `TranslationRepository`.

---

### [Component] UI Screens

#### [NEW] [MainScreen.kt](file:///C:/Users/Sak/AndroidStudioProjects/Traductor/app/src/main/java/com/sakatoon/traductor/ui/screens/MainScreen.kt)
The primary UI with:
- Source and Target language selection.
- Input text field.
- Translated text display.
- Buttons for Speech-to-Text (Microphone) and Text-to-Speech (Speaker).
- Navigation to Settings.

#### [NEW] [SettingsScreen.kt](file:///C:/Users/Sak/AndroidStudioProjects/Traductor/app/src/main/java/com/sakatoon/traductor/ui/screens/SettingsScreen.kt)
A screen to:
- List all supported languages.
- Show which models are downloaded.
- Download or delete language models.

---

### [Component] Main Activity & Navigation

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Sak/AndroidStudioProjects/Traductor/app/src/main/java/com/sakatoon/traductor/MainActivity.kt)
Set up Jetpack Navigation to switch between `MainScreen` and `SettingsScreen`. Initialize the repositories and provide them to the ViewModels (or use a simple DI approach).

---

## Verification Plan

### Automated Tests
- I will verify the build success after implementation.
- (Optional) Implement unit tests for `TranslationViewModel` if requested.

### Manual Verification
- Run the app on a device/emulator.
- Grant microphone permission.
- Download English and Spanish models.
- Test translation from English to Spanish (and vice versa).
- Test speech-to-text and text-to-speech.
