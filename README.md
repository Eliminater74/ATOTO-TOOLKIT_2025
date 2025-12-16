# ATOTO Toolkit 🚗💨

[![Hits](https://hits.seeyoufarm.com/api/count/incr/badge.svg?url=https%3A%2F%2Fgithub.com%2FEliminater74%2FATOTO-TOOLKIT&count_bg=%2379C83D&title_bg=%23555555&icon=&icon_color=%23E7E7E7&title=visitors&edge_flat=false)](https://hits.seeyoufarm.com)

The **ATOTO Toolkit** is an all-in-one comprehensive utility app designed specifically for **ATOTO S8 (Gen 2)** Android head units (and compatible devices). It provides essential tools to unlock the full potential of your car's infotainment system, streamlining customization, debloating, and optimization.

## ✨ Key Features

### 🚀 Device Dashboard
Get a real-time overview of your head unit:
- **Hardware ID**: Instantly verifies if you are running on genuine S8 hardware (e.g., `UMS512`, `SPRD`).
- **Network Status**: Displays current IP address for wireless debugging.
- **Root Status**: Checks for Root and Shizuku availability.

### 🗑️ Smart Debloater
Remove pre-installed junk safely and easily.
- **Root / Shizuku Mode**: Bulk "Disable" or "Uninstall" multiple apps at once.
- **Manual "Safe Mode"**: For non-root users, select an app and get redirected straight to its System Settings to disable it manually.
- **Protection**: Built-in "Safe List" prevents you from accidentally removing critical system components.

### 📡 Wireless ADB Manager
Connect to your head unit wirelessly without cables.
- **Android 11+**: Direct shortcut to native Wireless Debugging settings.
- **Android 10 (S8 Standard)**: **Exclusive Feature!** Uses Root to force-enable ADB over TCP/IP (Port 5555) with a simple toggle switch.
- **Connection Guide**: Step-by-step instructions for connecting from your PC.

### 📻 Radio & App Guides
Curated rankings and recommendations for the best automotive apps:
- **Radio Replacements**: The best alternatives to the stock radio app, ranked by hardware compatibility. (Top Pick: **NavRadio+**).
- **Essential Apps**: Hand-picked navigation, OBD2 diagnostics, and music apps that work best on head units.

### 🛡️ Safety First
- **Startup Protection**: Automatically checks your device hardware on launch. If you aren't on an ATOTO S8, it warns you to prevent potential issues.
- **Non-Destructive Defaults**: Defaults to "Disable" rather than "Uninstall" to ensure reversibility.

---

## 🛠️ How to Build

This project is built using standard Android development tools.

### Prerequisites
- **Android Studio** (Koala or newer recommended)
- **JDK 17** (or newer)

### Build Steps
1.  **Clone the repository**:
    ```bash
    git clone https://github.com/Eliminater74/ATOTO-TOOLKIT.git
    ```
2.  **Open in Android Studio**:
    - Select `File > Open` and choose the cloned directory.
3.  **Sync Gradle**:
    - Allow Android Studio to download dependencies and sync the project.
4.  **Build APK**:
    - Go to `Build > Build Bundle(s) / APK(s) > Build APK(s)`.
    - The output APK will be in `app/build/outputs/apk/debug/`.

---

## 🔮 Future Improvements

We are constantly working to improve the toolkit. Upcoming features include:

- **Full Backup/Restore**: One-click backup of key app configurations (Launchers, Radio settings).
- **Custom Debloat Profiles**: Create and share your own lists of apps to remove.
- **"Danger Zone"**: Advanced tools for power users (DPI changer, hidden settings access).
- **Log Viewer**: On-device viewing of logcat for debugging without a PC.

---

## 📝 Changelog

See [CHANGELOG.md](./CHANGELOG.md) for a complete history of changes and updates.

---

*Disclaimer: This app is a community tool and is not officially affiliated with ATOTO. Use root tools with caution.*
