# Satori - Your Guide to Cognitive Wellbeing

**Satori** is an advanced multiplatform application (Kotlin Multiplatform) designed for neurodivergent individuals (ADHD, Autism) and anyone looking to train focus, monitor overstimulation, and build healthy routines.

## 🌟 Key Features

### 🧠 Training & Diagnostics
- **Reaction Test:** Precise measurement of reaction time to visual stimuli.
- **Mind Challenges:** Cognitive games such as *Color Clash* (mindfulness) and *Memory Game* (working memory).
- **Self-Assessment:** Daily monitoring of attention, memory, and executive functions.

### 📊 Advanced Analytics & AI
- **Gemini AI Analysis:** An intelligent assistant that analyzes your results, moods, and routines to provide personalized insights and advice.
- **Circadian Performance:** Charts showing the hours when your brain works most effectively.
- **Mood Map:** Visualization of the correlation between wellbeing and completed tasks.
- **Data Export:** Ability to download full history (CSV) for analysis with a doctor or therapist.

### 🧘 Overstimulation Management
- **Breathing Tool:** An interactive animation guiding you through calming breathing sessions.
- **Knowledge Base:** A collection of stress reduction techniques and tips for handling sensory overload.
- **Routine System:** Habit building with personalized icons and notifications.

### 🎨 Accessibility & UX
- **Gamification:** A "Streak" system (days in a row) to motivate consistency.
- **Daily Satori Score:** A holistic daily goal (0-100) based on your activities and wellbeing.
- **Full Accessibility:** High contrast mode, large font support, and control over animations.
- **Multiplatform:** A consistent experience on Android, iOS, and Web (Wasm).

## 🛠 Tech Stack

- **Kotlin Multiplatform (KMP):** Sharing business logic and UI.
- **Compose Multiplatform:** Declarative user interface.
- **Gemini AI SDK:** Integration with Google's language model for intelligent analysis.
- **SQLDelight:** Local SQLite database with static typing.
- **Koin:** Dependency Injection.
- **Ktor:** Network communication and server synchronization.
- **KotlinX Datetime:** Precise time and time zone management.

## 📁 Project Structure

* [/composeApp](./composeApp/src) - Shared Compose UI (Android, iOS, Web).
* [/shared](./shared/src) - App Core: Database, AI Service, Repositories, Notifications.
* [/server](./server/src/main/kotlin) - Ktor backend for data synchronization.

## ⚖️ Legal & IP Ownership

Satori follows a strict "Human-First" coding policy to ensure intellectual property clarity and user data protection.
- **Copyright:** All core logic and architectural structures are human-authored.
- **AI Usage:** Generative AI is used strictly as a productivity aid for boilerplate and UI layouts.
- **Transparency:** See [LEGAL.md](./LEGAL.md) for our full AI Disclosure and IP statement.
- **License:** Licensed under [Apache 2.0](./LICENSE).

---

## 🛠 Installation & Running

### Android
```shell
./gradlew :composeApp:assembleDebug
```

### iOS
Open the `/iosApp` directory in Xcode or run directly from Android Studio on macOS.

### Web (Wasm)
```shell
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

---

*The Satori project is constantly being developed to provide the best tools supporting digital and cognitive hygiene.*
