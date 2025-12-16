package dev.eliminater.atoto_toolkit

import android.os.Build

object DeviceUtils {
    /**
     * Checks if the device is likely an ATOTO unit.
     * Looks for "ATOTO" in manufacturer, brand, or model.
     */
    fun isAtotoDevice(): Boolean {
        val manufacturer = Build.MANUFACTURER.uppercase()
        val brand = Build.BRAND.uppercase()
        val model = Build.MODEL.uppercase()
        val product = Build.PRODUCT.uppercase()
        val board = Build.BOARD.uppercase()
        
        // Check for branding "ATOTO" OR known hardware identifiers for S8 units (Unisoc/Spreadtrum)
        return manufacturer.contains("ATOTO") || 
               brand.contains("ATOTO") || 
               model.contains("ATOTO") ||
               product.contains("ATOTO") ||
               // S8 Gen 2 Hardware Signatures (Unisoc 7862 / Ums512)
               model.contains("UMS512") || 
               board.contains("UMS512") ||
               model.contains("SPRD")
    }

    /**
     * Checks if the device is specifically an ATOTO S8 series.
     * S8 Gen 2 uses the Unisoc 7862 (UMS512) chipset.
     */
    fun isS8Device(): Boolean {
        val model = Build.MODEL.uppercase()
        val board = Build.BOARD.uppercase()
        
        return isAtotoDevice() && (
            model.contains("S8") || 
            model.contains("UMS512") || // Identifying raw hardware name
            board.contains("UMS512")
        )
    }
}
