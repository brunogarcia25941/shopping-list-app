# 🛒 Family Shopping List (Cross-Platform)

A modern, real-time, cross-platform shopping list application built entirely with **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**.

This app allows families to share a single, synchronized shopping list without the friction of creating accounts, managing passwords, or sharing emails. You simply invent a "Family Code" (e.g., `SMITH2026`) and share it. Anyone using that code will see and update the exact same list in real-time.

🌐 **Try the Web Version here:** ! https://family-shopping-list-maoz.onrender.com
📱 **Download on Google Play:** [Insert Play Store Link Here] *(Coming Soon)*

---

## 📄 License & Privacy
* **Privacy Policy:** https://sites.google.com/view/family-shoppinglist
* **Author:** Bruno Garcia

---

## ✨ Key Features

* ⚡ **Real-Time Synchronization:** Powered by WebSockets. Add, check, or delete items on your phone, and watch them update instantly on your family's devices.
* 👨‍👩‍👧‍👦 **Frictionless Onboarding:** No accounts required. Just a shared "Family Code".
* 🌐 **Kotlin/Wasm Web Support:** iPhone and PC users don't even need to install an app. They can access the fully functional Compose UI directly from their browser, powered by WebAssembly.
* 🚀 **Optimistic UI Updates:** The app updates the local UI instantly before the server even responds. If the network fails, it smoothly reverts the changes.
* 👆 **Modern Gestures:**
  * **Swipe-to-Delete:** With animated trash icons and a "Undo" Snackbar.
  * **Pull-to-Refresh:** Utilizing Material 3's new `PullToRefreshBox`.
* 📳 **Haptic Feedback:** Custom native vibrator interfaces implemented for Android and iOS for a premium feel.
* 🌍 **Bilingual Support:** Automatically adapts to English and Portuguese based on the device's language.
* 🛡️ **GDPR Compliant:** Fully integrated with Google AdMob and User Messaging Platform (UMP) for European privacy laws.

---

## 🛠️ Tech Stack & Architecture

This project strictly follows a **single-codebase** philosophy, maximizing code sharing across Android, iOS, and Web.

* **Language:** Kotlin
* **Frameworks:** Kotlin Multiplatform (KMP)
* **UI Toolkit:** Compose Multiplatform (Material 3)
* **Network & Real-Time:** Ktor Client (HTTP requests & WebSockets)
* **Serialization:** `kotlinx.serialization` (JSON parsing)
* **Web Target:** Kotlin/Wasm (WebAssembly)
* **Coroutines:** `kotlinx.coroutines` for asynchronous flow and state management.
* **Backend / Hosting:** Render (Static Site for Wasm, Web Service for Ktor backend).

---

## 🧠 Technical Highlights

### Smart Reconnection Protocol
To handle unstable mobile networks, the WebSocket client is wrapped in a resilient coroutine loop. It features a `20-second ping interval` to prevent server timeouts and a `3-second auto-reconnect delay` if the connection drops.

### Platform-Specific Implementations (`expect`/`actual`)
While 95% of the code is shared, native capabilities are seamlessly injected using Kotlin's `expect`/`actual` mechanism:
* **Image Compression:** Native Skia for Web, Android Bitmap for Mobile.
* **Haptic Engine:** `Vibrator` service on Android, `UIImpactFeedbackGenerator` on iOS.
* **Clipboard & Share:** Native OS sharing menus for mobile, Javascript interop (`js()`) for Web.

---

## 🚀 How to Run Locally

### Prerequisites
* Android Studio (latest version) or IntelliJ IDEA.
* Java JDK 17+.

### Android
1. Open the project in Android Studio.
2. Select the `composeApp` run configuration.
3. Choose an emulator or physical device.
4. Hit Run (Shift + F10).

### Web (Wasm)
To run the web application locally in your browser:
```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

