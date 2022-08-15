package com.fattmerchant.fmsampleclient

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/**
 * An object that can request permissions. This should be an Activity or a Fragment
 *
 * ## Usage
 * After declaring conformance to PermissionsManager, add the `permissionRequestLauncher` and
 * the `permissionsRequestLauncherCallback`. It's very important that both of those are implemented
 * like this:
 *
 * ```
 *  override var permissionRequestLauncherCallback: ((Boolean) -> Unit)? = null
 *
 *  override var permissionRequestLauncher
 *      = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
 *      permissionRequestLauncherCallback?.invoke(isGranted)
 *  }
 * ```
 *
 * **In order for this to work properly, the `permissionRequestLauncher` MUST invoke the
 * `permissionRequestLauncherCallback`!!**
 */
interface PermissionsManager {
    var permissionRequestLauncher: ActivityResultLauncher<String>
    var permissionRequestLauncherCallback: ((Boolean) -> Unit)?
    fun getActivity(): AppCompatActivity?
    fun getContext(): Context?
}

/**
 * Ensures that the given `permission` is granted, and only then invokes `performWhenAllowed`.
 *
 * This handles everything from asking for the permissions to showing the alert dialogs informing
 * the user about why they should grant the permission
 */
fun PermissionsManager.runIfPermissionGranted(
    permission: String,
    permissionRationaleTitle: Int,
    permissionRationaleMessage: Int,
    permissionDeniedTitle: Int,
    permissionDeniedMessage: Int,
    performWhenAllowed: () -> (Unit)
) {

    val activity: Activity = getActivity() ?: return
    val ctx: Context = getContext() ?: return
    when {
        // Permission is already granted
        ContextCompat.checkSelfPermission(ctx, permission) == PackageManager.PERMISSION_GRANTED -> {
            performWhenAllowed()
        }

        // Android is requesting that we tell the user why we want permissions
        activity.shouldShowRequestPermissionRationale(permission) -> {
            AlertDialog.Builder(ctx)
                .setTitle(permissionRationaleTitle)
                .setMessage(permissionRationaleMessage)
                .setNegativeButton("No thanks") { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("Okay") { dialog, _ ->
                    permissionRequestLauncherCallback = { isGranted: Boolean ->
                        if (isGranted) {
                            performWhenAllowed()
                        }
                    }
                    permissionRequestLauncher.launch(permission)
                    dialog.dismiss()
                }
                .create()
                .show()
        }

        else -> {
            permissionRequestLauncherCallback = { isGranted: Boolean ->
                if (isGranted) {
                    performWhenAllowed()
                } else if (activity.shouldShowRequestPermissionRationale(permission)) {
                    // The user denied the permission, but we can still ask again because maybe
                    // they don't understand _why_ it's so important that they give us
                    // permission
                    runIfPermissionGranted(
                        permission,
                        permissionRationaleTitle,
                        permissionRationaleMessage,
                        permissionDeniedTitle,
                        permissionDeniedMessage,
                        performWhenAllowed
                    )
                } else {
                    // If we've asked for the permission, AND it's not granted, AND
                    // we should not show the permission rationale, then we can safely assume
                    // that the user has chosen to never see this permission again. Any further
                    // attempts to request the permission are automatically denied by
                    // Android so our only remedy is to politely ask the user to grant the
                    // permission in the settings app
                    AlertDialog.Builder(ctx)
                        .setTitle(permissionDeniedTitle)
                        .setMessage(permissionDeniedMessage)
                        .setNegativeButton("No thanks") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setPositiveButton("Settings") { dialog, _ ->
                            dialog.dismiss()
                            activity.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID)))
                        }
                        .create()
                        .show()
                }
            }
            permissionRequestLauncher.launch(permission)
        }
    }
}
