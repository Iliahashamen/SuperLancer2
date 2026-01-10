# 🏎️ Super Lancer 2

**Super Lancer 2** is a high-speed, retro-themed arcade survival game for Android. The player controls a race car to dodge incoming rocks and collect rare heart power-ups to survive as long as possible.

## 📱 Gameplay Features
* **Retro Aesthetic:** 8-bit style fonts and vector assets.
* **Dynamic Difficulty:** The game gets faster as your score increases.
* **Health System:** Start with 3 lives. Shake animation and vibration on impact.
* **Power-Ups:** Rare hearts spawn (8% chance) when lives are critical (< 3).
* **High Scores:** Tracks the top 5 runs in the current session.

## 🛠️ Technical Implementation
This project was built to demonstrate modern Android development practices:
* **Language:** Kotlin.
* **UI/Layout:** `ConstraintLayout` using **Horizontal Bias** for lane movement (No absolute X/Y coordinates).
* **Animation:** `ObjectAnimator` for smooth obstacle movement and car "shake" effects.
* **Assets:** Custom Vector Drawables (XML) for infinite scaling and performance.

## 🎮 How to Play
1.  Launch the app and enter your name.
2.  Use the **"<"** and **">"** buttons to switch between the 3 lanes.
3.  Avoid the **Rocks** (Grey, Brown, Dark).
4.  Catch the **Hearts** if you are low on health!

## 🚀 Future Roadmap (Phase 3)
We are actively planning the next major update, **Super Lancer 3**, which will include:
* **[ ] 5-Lane Highway:** Expanding the road width for more complex dodging patterns.
* **[ ] Gyroscope Support:** Tilt the phone to steer instead of using buttons.
* **[ ] Audio Engine:** Adding background music and retro crash sound effects.
* **[ ] Persistent Data:** Saving High Scores permanently using `SharedPreferences`.

## 💻 Installation
1.  Clone this repository.
2.  Open in **Android Studio**.
3.  Sync Gradle and Run on an Emulator or Physical Device.