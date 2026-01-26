package com.fattmerchant.android.chipdna

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import java.security.MessageDigest

/**
 * Utility for extracting the app's certificate fingerprint for Tap to Pay authentication.
 */
internal object CertificateUtils {
    
    /**
     * Extracts the SHA-256 certificate fingerprint from the app's signing certificate.
     * 
     * @param context Application context
     * @return SHA-256 fingerprint as hex string, or null if unable to extract
     */
    fun getCertificateFingerprint(context: Context): String? {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }
            
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }
            
            if (signatures.isNullOrEmpty()) {
                return null
            }
            
            // Get the first signature and compute SHA-256
            val signature = signatures[0]
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(signature.toByteArray())
            
            // Convert to hex string format (colon-separated)
            digest.joinToString(":") { byte ->
                "%02X".format(byte)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
