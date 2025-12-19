package dev.eliminater.atoto_toolkit

import androidx.compose.ui.graphics.Color

enum class SafetyLevel {
    SAFE,       // Green: Safe to disable
    CAUTION,    // Yellow: Warning, might break features
    UNSAFE,     // Red: Do not touch (System/Boot/Critical)
    UNKNOWN     // Default/Gray
}

data class PackageInfo(
    val description: String,
    val safety: SafetyLevel
)

object PackageDB {
    val KNOWN_PACKAGES = mapOf(
        // --- FYT / ATOTO Core (UNSAFE) ---
        "com.syu.protocolupdate" to PackageInfo("FYT Protocol Updater. Critical for MCU communication. DO NOT REMOVE.", SafetyLevel.UNSAFE),
        "com.syu.cs" to PackageInfo("Core vehicle service. Handles car data logic.", SafetyLevel.UNSAFE),
        "com.syu.ms" to PackageInfo("Media service. Required for radio/music audio handling.", SafetyLevel.UNSAFE),
        "com.syu.ps" to PackageInfo("Power service. Handles sleep/wake and ACC capability.", SafetyLevel.UNSAFE),
        "com.syu.ss" to PackageInfo("System service. Essential vendor glue.", SafetyLevel.UNSAFE),
        "com.syu.us" to PackageInfo("USB service. Handles USB mounting and connection.", SafetyLevel.UNSAFE),
        "com.syu.canbus" to PackageInfo("CANBUS Decoder bridge. Needed for steering wheel controls, AC info, door status.", SafetyLevel.UNSAFE),
        "com.syu.bt" to PackageInfo("Bluetooth stack (Phone/A2DP). Handles calls. Keep if you use HU for calls.", SafetyLevel.UNSAFE),
        "com.syu.steer" to PackageInfo("Steering Wheel Control mapping app. Keep until keys are mapped.", SafetyLevel.UNSAFE),
        "com.syu.settings" to PackageInfo("ATOTO/Factory Settings menu. Essential for car configuration.", SafetyLevel.UNSAFE),
        "com.syu.rearcamera" to PackageInfo("Rear Camera handler. Handles reverse signal trigger.", SafetyLevel.UNSAFE),
        "com.atoto.keepaliveservice" to PackageInfo("ATOTO Keep-Alive service. Prevents critical apps from being killed.", SafetyLevel.UNSAFE),
        "org.atoto.gps" to PackageInfo("ATOTO GPS Hardware handler.", SafetyLevel.UNSAFE),

        // --- Camera / Driving Features (CAUTION) ---
        "com.syu.carmark" to PackageInfo("Parking lines / 360 overlay helper. May be needed for camera overlays.", SafetyLevel.CAUTION),
        "com.syu.rightcamera" to PackageInfo("Right-side blindspot camera viewer. Safe to remove if you don't have a side camera.", SafetyLevel.SAFE),
        "com.syu.frontvideo" to PackageInfo("Front DVR/Camera viewer. Safe to remove if no front cam used.", SafetyLevel.SAFE),
        "com.syu.gesture" to PackageInfo("Touch screen gestures. Disable if you don't use screen gestures.", SafetyLevel.SAFE),
        "com.syu.calibration" to PackageInfo("Touch/Screen Calibration tool. Keep just in case, or restore if needed.", SafetyLevel.CAUTION),

        // --- ATOTO / OEM Specific (MIXED) ---
        "com.atoto.speechtotext" to PackageInfo("ATOTO Voice Control. Safe to remove if you use Google Assistant.", SafetyLevel.SAFE),
        "com.atoto.carsysteminfo" to PackageInfo("System Info Card. Shows storage/ram. Safe to remove.", SafetyLevel.SAFE),
        "com.aidl.atoto.store" to PackageInfo("ATOTO App Store. Often outdated. Safe to remove.", SafetyLevel.SAFE),
        "com.atoto.uvc2camera" to PackageInfo("External USB Camera Handler. Safe if you don't use a dashcam.", SafetyLevel.SAFE),
        "com.hugoteam.airtalk_wireless" to PackageInfo("Wireless CarPlay/Android Auto Service. Removing THIS breaks wireless phone projection.", SafetyLevel.UNSAFE),
        "com.synmoon.carkit" to PackageInfo("Bluetooth Phone Link service. Required for some hands-free features.", SafetyLevel.CAUTION),
        
        // --- Apps / Bloat (SAFE) ---
        "com.syu.carlink" to PackageInfo("PhoneLink / CarPlay / Android Auto bridge. Remove ONLY if you don't use CarPlay/AA.", SafetyLevel.CAUTION),
        "net.easyconn" to PackageInfo("EasyConnection screen mirroring app. Obsolete/Junk if using CarPlay/AA.", SafetyLevel.SAFE),
        "com.syu.music" to PackageInfo("Stock Music Player. Safe to remove if you use Spotify/etc.", SafetyLevel.SAFE),
        "com.syu.video" to PackageInfo("Stock Video Player. Safe to remove.", SafetyLevel.SAFE),
        "com.syu.gallery" to PackageInfo("Stock Gallery app. Safe to remove.", SafetyLevel.SAFE),
        "com.syu.filemanager" to PackageInfo("Stock File Manager. Safe to remove if you use a 3rd party one.", SafetyLevel.SAFE),
        "com.syu.carradio" to PackageInfo("Stock FM/AM Radio. Safe to remove if you don't listen to radio or use NavRadio+.", SafetyLevel.SAFE),
        "com.syu.radio" to PackageInfo("Alternative Stock Radio package. Safe to remove.", SafetyLevel.SAFE),
        "com.syu.eq" to PackageInfo("Stock Equalizer/DSP. Remove only if you don't use built-in audio processing.", SafetyLevel.CAUTION),
        "com.syu.av" to PackageInfo("AV-IN (RCA Input) Viewer. Safe to remove if unused.", SafetyLevel.SAFE),
        "com.ex.dabplayer.pad" to PackageInfo("DAB+ Radio Player. Safe to remove if no DAB dongle.", SafetyLevel.SAFE),
        "com.hualai" to PackageInfo("Generic Chinese bloatware / Camera test tool. Safe to remove.", SafetyLevel.SAFE),
        
        // --- Third Party / Generic Bloat (SAFE) ---
        "com.google.android.apps.maps" to PackageInfo("Google Maps. Safe to remove if you use Waze/etc.", SafetyLevel.SAFE),
        "com.google.android.youtube" to PackageInfo("YouTube.", SafetyLevel.SAFE),
        "com.android.chrome" to PackageInfo("Google Chrome.", SafetyLevel.SAFE),
        "com.google.android.googlequicksearchbox" to PackageInfo("Google App / Assistant. Heavy resource usage.", SafetyLevel.SAFE),
        "com.google.android.tts" to PackageInfo("Text-to-Speech. Needed for Voice navigation prompts.", SafetyLevel.CAUTION),
        "com.netflix.mediaclient" to PackageInfo("Netflix. Safe to remove.", SafetyLevel.SAFE),
        "com.facebook.katana" to PackageInfo("Facebook. Safe to remove.", SafetyLevel.SAFE),
        "com.spotify.music" to PackageInfo("Spotify. Safe to remove.", SafetyLevel.SAFE),
        
        // --- Android System (UNSAFE) ---
        "android" to PackageInfo("Android System Framework. DO NOT TOUCH.", SafetyLevel.UNSAFE),
        "com.android.systemui" to PackageInfo("System UI (Status bar, buttons). DO NOT TOUCH.", SafetyLevel.UNSAFE),
        "com.android.settings" to PackageInfo("Android Settings. DO NOT TOUCH.", SafetyLevel.UNSAFE),
        "com.android.vending" to PackageInfo("Google Play Store. Keep for app updates.", SafetyLevel.CAUTION),
        "com.google.android.gms" to PackageInfo("Google Play Services. Many apps depend on this.", SafetyLevel.UNSAFE),
        "com.google.android.gsf" to PackageInfo("Google Services Framework.", SafetyLevel.UNSAFE),
        "com.android.inputmethod.latin" to PackageInfo("AOSP Keyboard. Keep unless you have another keyboard installed.", SafetyLevel.CAUTION),
        
        // --- Spreadtrum / Unisoc Vendor (UNSAFE) ---
        "com.sprd.systemupdate" to PackageInfo("Spreadtrum System Update. Critical firmware OTA handler.", SafetyLevel.UNSAFE),
        "com.sprd.logmanager" to PackageInfo("Spreadtrum Logger. Safe to ignore, risky to remove.", SafetyLevel.CAUTION),
        "com.spreadtrum.sgps" to PackageInfo("Spreadtrum GPS Assist. Improving lock time.", SafetyLevel.UNSAFE),
        "com.spreadtrum.ims" to PackageInfo("VoLTE/IMS Service. Likely unused on WiFi tablets but part of modem stack.", SafetyLevel.UNSAFE)
    )
}
