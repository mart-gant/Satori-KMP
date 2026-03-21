# Satori - Reaction Time Trainer

**Satori** is a multiplatform application (Kotlin Multiplatform) designed for testing and training reaction time. The project demonstrates the use of modern technologies in the Kotlin ecosystem, offering a consistent experience across Android, iOS, and Web platforms.

## 🚀 Features

- **Reaction Test:** High-precision measurement of reaction time to visual stimuli.
- **Ranking System:** Your results are evaluated on a scale ranging from "Sloth" to "Ninja".
- **Result History:** View your progress and statistics in a dedicated reports module.
- **Personalization:** Set your nickname and customize the app's appearance.
- **Accessibility:** Support for high contrast mode and large font sizes.
- **Multiplatform:** Shared business logic and UI (Compose Multiplatform) across multiple systems.

## 🛠 Tech Stack

- **Kotlin Multiplatform (KMP):** Code sharing across platforms.
- **Compose Multiplatform:** Declarative UI for Android, iOS, and Web.
- **Koin:** Dependency Injection.
- **SQLDelight:** Multiplatform database support (SQLite).
- **Ktor:** Network communication and server-side logic.
- **KotlinX Coroutines & Serialization:** Asynchronous programming and data processing.

## 📁 Project Structure

* [/composeApp](./composeApp/src) - Shared Compose Multiplatform UI code.
* [/shared](./shared/src) - Shared business logic, database, and data models.
* [/server](./server/src/main/kotlin) - Ktor-based server application.
* [/iosApp](./iosApp/iosApp) - Entry point for the iOS platform (SwiftUI).

---

## 🛠 Build and Run

### Android
```shell
./gradlew :composeApp:assembleDebug
```

### Server (Ktor)
```shell
./gradlew :server:run
```

### Web (Wasm / JS)
- **Wasm:** `./gradlew :composeApp:wasmJsBrowserDevelopmentRun`
- **JS:** `./gradlew :composeApp:jsBrowserDevelopmentRun`

### iOS
Open the [/iosApp](./iosApp) directory in Xcode or use the run configuration in Android Studio (requires macOS).

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html) and [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform).
