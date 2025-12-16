# Changelog

All notable changes to the **ATOTO Toolkit** project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased] - 2025-12-16

### Added
- **Device Compatibility Check**: 
    - Automatically detects if the device is an ATOTO S8 (Gen 2) or compatible hardware (e.g., `UMS512`, `SPRD` chipsets).
    - Displays a dismissible warning on startup if running on unsupported hardware to prevent misuse.
- **Wireless ADB Support**:
    - **Android 10 (S8 Standard)**: Added a Root-powered toggle ("Force Port 5555") to enable Wireless ADB on devices that lack the native feature.
    - **Android 11+**: Added direct links to the native Wireless Debugging settings.
    - Included a Connection Manager guide for both Root and Non-Root users.
- **Enhanced Debloater**:
    - **Manual Safe Mode**: Non-root users can now select an app and click "Manual Disable", which redirects to the system app settings for safe disabling.
    - **Root/Shizuku Power Mode**: Bulk "Disable" and "Uninstall" options for advanced users.
    - Added "Safe" vs "Unsafe" package protection logic to prevent accidental system bricking.
- **Radio Replacements Guide**:
    - Comprehensive ranked list of radio apps.
    - **NavRadio+** ranked #1 with automatic detection for both Paid and Free versions.
    - Detailed Pros/Cons for each radio option.
- **App Recommendations**:
    - New "Apps" section featuring curated lists for Navigation (Waze, Maps), OBD2 (Torque, Car Scanner), and Music.
- **Sidebar Navigation**:
    - Implemented a modern, scrollable sidebar for better ergonomics on head units.
    - Reordered categories: Launchers -> Radio -> Apps -> Debloat -> Backup.
- **Status Dashboard**:
    - Home screen now shows real-time device stats (Model, Android Version, Root Status, IP Address).

### Changed
- **UI/UX Overhaul**:
    - Switched to a premium "Dark/Glass" aesthetic using Material3.
    - Added custom gradients and high-contrast icons for automotive visibility.
- **Navigation Logic**: 
    - "Radio" section moved directly below "Launchers" for logical grouping of replacement tools.

### Fixed
- **Compilation Errors**: Resolved issues with missing Compose imports (`remember`, `LocalContext`, `Modifier`) in various screens.
- **Hardware Detection**: Fixed potential false negatives for S8 devices identifying as generic Spreadtrum (`SPRD`) units.

## [0.1.0] - 2025-12-14
### Initial Release
- Basic project structure.
- Placeholder screens for main tools.
- Initial implementation of Root/Shizuku shell executor.
