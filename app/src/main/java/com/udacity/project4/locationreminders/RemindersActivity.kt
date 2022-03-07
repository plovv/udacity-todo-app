package com.udacity.project4.locationreminders

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R

/**
 * The RemindersActivity that holds the reminders fragments
 */

class RemindersActivity : AppCompatActivity() {

    private val REQUEST_BACKGROUND_LOCATION_PERMISSION_RESULT_CODE = 33
    private val REQUEST_FOREGROUND_LOCATION_PERMISSIONS_REQUEST_CODE = 34

    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    private val requestLocationSettings = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
        if (it.resultCode != Activity.RESULT_OK) {
            Toast.makeText(this, "Location tracking must be enabled for the app to function properly.", Toast.LENGTH_SHORT).show()
            finish() // exits app
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)
    }

    override fun onStart() {
        super.onStart()
        checkForPermissionsAndLocationSettings()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {

                findNavController(R.id.nav_host_fragment).popBackStack()

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (
            grantResults.isEmpty() ||
            (
                requestCode == REQUEST_FOREGROUND_LOCATION_PERMISSIONS_REQUEST_CODE &&
                grantResults[0] == PackageManager.PERMISSION_DENIED
            ) ||
            (
                requestCode == REQUEST_BACKGROUND_LOCATION_PERMISSION_RESULT_CODE &&
                grantResults[0] == PackageManager.PERMISSION_DENIED
            )
        ) {
            // Permission denied.
            Snackbar.make(findViewById(R.id.remainders_activity_root), R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.settings) {
                    // Displays App settings screen.
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", applicationContext.packageName, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        } else if (requestCode == REQUEST_FOREGROUND_LOCATION_PERMISSIONS_REQUEST_CODE) {
            if (runningQOrLater) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), REQUEST_BACKGROUND_LOCATION_PERMISSION_RESULT_CODE)
            } else {
                checkForPermissionsAndLocationSettings()
            }
        } else {
            checkForPermissionsAndLocationSettings()
        }
    }

    @TargetApi(29)
    private fun checkForPermissionsAndLocationSettings() {
        if (!foregroundAndBackgroundLocationPermissionApproved()) {
            requestForegroundAndBackgroundLocationPermissions()
        } else {
            checkDeviceLocationSettings()
        }
    }

    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } else {
                true
            }

        return foregroundLocationApproved && backgroundPermissionApproved
    }

    @TargetApi(29 )
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (foregroundAndBackgroundLocationPermissionApproved())
            return

        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_FOREGROUND_LOCATION_PERMISSIONS_REQUEST_CODE)
    }

    private fun checkDeviceLocationSettings() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(this)
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                try {
                    requestLocationSettings.launch(IntentSenderRequest.Builder(exception.resolution.intentSender).build())
                } catch (sendEx: IntentSender.SendIntentException) {
                    //Log.d(TAG, "Error geting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(findViewById(R.id.remainders_activity_root), R.string.location_required_error, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok) {
                        checkDeviceLocationSettings()
                    }
                    .show()
            }
        }
    }

}
