package com.simple.hotspotcreator

import android.Manifest
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.simple.hotspotcreator.databinding.ActivityMainBinding
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

import android.os.Build
import android.provider.Settings
import android.util.Log
import android.net.wifi.WifiConfiguration

import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.LocalOnlyHotspotReservation
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar

const val PERMISSION_REQUEST_LOCATION = 0

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.bCreateHotspot.setOnClickListener {
//            Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
//            requestCameraPermission()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Step 1: Enable the location settings use Google Location Service
                // Step 2: https://stackoverflow.com/questions/29801368/how-to-show-enable-location-dialog-like-google-maps/50796199#50796199
                // Step 3: If OK then check the location permission and enable hotspot
                // Step 4: https://stackoverflow.com/questions/46843271/how-to-turn-off-wifi-hotspot-programmatically-in-android-8-0-oreo-setwifiapen
//                enableLocationSettings();
//                return
            }
        }
    }

    private fun checkHostspot() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startHotspot()
        } else {
            Toast.makeText(this, "Feature is not available", Toast.LENGTH_SHORT).show()
        }
    }


    private fun requestCameraPermission() {
        // Permission has not been granted and must be requested.
        if (shouldShowRequestPermissionRationaleCompat(Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
            binding.parent.showSnackbar(
                "R.string.camera_access_required",
                Snackbar.LENGTH_INDEFINITE, "R.string.ok"
            ) {
                requestPermissionsCompat(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSION_REQUEST_LOCATION
                )
            }

        } else {
            binding.parent.showSnackbar(
                "R.string.camera_permission_not_available",
                Snackbar.LENGTH_SHORT
            )

            // Request the permission. The result will be received in onRequestPermissionResult().
            requestPermissionsCompat(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_LOCATION)
        }
    }

    private fun showCameraPreview() {
        // Check if the Camera permission has been granted
        if (checkSelfPermissionCompat(Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is already available, start camera preview
            binding.parent.showSnackbar(
                "R.string.camera_permission_available",
                Snackbar.LENGTH_SHORT
            )
            checkHostspot()
        } else {
            // Permission is missing and must be requested.
            requestCameraPermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            // Request for camera permission.
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start camera preview Activity.
                binding.parent.showSnackbar(
                    "R.string.camera_permission_granted",
                    Snackbar.LENGTH_SHORT
                )
                checkHostspot()
            } else {
                // Permission request was denied.
                binding.parent.showSnackbar(
                    "R.string.camera_permission_denied",
                    Snackbar.LENGTH_SHORT
                )
            }
        }
    }


    private fun showWritePermissionSettings(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && Build.VERSION.SDK_INT < Build.VERSION_CODES.O
        ) {
            if (!Settings.System.canWrite(this)) {
                Log.v("DANG", " " + !Settings.System.canWrite(this))
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = Uri.parse("package:" + this.packageName)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                this.startActivity(intent)
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            return true //Permission already given
        } else {
            return false
        }
//        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
//        return true //Permission already given
    }

    private lateinit var wifiManager: WifiManager
    var currentConfig: WifiConfiguration? = null
    var hotspotReservation: LocalOnlyHotspotReservation? = null

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun startHotspot() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        Toast.makeText(this, "Starting hotspot", Toast.LENGTH_SHORT).show()
        wifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.startLocalOnlyHotspot(wifiCallbacks, Handler(Looper.getMainLooper()))
    }

    private val wifiCallbacks = @RequiresApi(Build.VERSION_CODES.O)
    object : WifiManager.LocalOnlyHotspotCallback() {
        override fun onStarted(reservation: LocalOnlyHotspotReservation?) {
            super.onStarted(reservation)
            hotspotReservation = reservation;
            currentConfig = hotspotReservation?.getWifiConfiguration();

            Log.v(
                "DANG", "THE PASSWORD IS: "
                        + currentConfig?.preSharedKey
                        + " \n SSID is : "
                        + currentConfig?.SSID
            );

            hotspotDetailsDialog()
        }

        override fun onStopped() {
            super.onStopped()
            Log.v("DANG", "Local Hotspot Stopped")
        }

        override fun onFailed(reason: Int) {
            super.onFailed(reason)
            Log.v("DANG", "Local Hotspot failed to start")
        }
    }

    fun hotspotDetailsDialog() {
        Log.v(
            "TAG", "context.getString(R.string.hotspot_details_message)" + "\n"
                    + "context.getString(R.string.hotspot_ssid_label) "
                    + " " + currentConfig?.SSID + "\n"
                    + "context.getString(R.string.hotspot_pass_label)"
                    + " " + currentConfig?.preSharedKey
        )
    }
}
