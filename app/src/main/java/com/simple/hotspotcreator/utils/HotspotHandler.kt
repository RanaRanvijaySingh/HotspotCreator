package com.simple.hotspotcreator.utils

import android.Manifest
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import java.lang.Exception
import javax.inject.Inject

class HotspotHandler @Inject constructor(private val activity: AppCompatActivity) {
    private var manager: WifiManager.LocalOnlyHotspotReservation? = null
    private var isEnabled = false
    private lateinit var callback: HotspotHandlerCallbacks

    fun setHotspotHandlerCallback(callback: HotspotHandlerCallbacks) {
        this.callback = callback
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun enableLocationSettings() {
        val locationRequest = LocationRequest()
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest).setAlwaysShow(false)
        val task = LocationServices.getSettingsClient(activity)
            .checkLocationSettings(builder.build())
        task.addOnCompleteListener { task1: Task<LocationSettingsResponse?> ->
            try {
                // Call function to check if the location is working
                task1.getResult(ApiException::class.java)
                updateHotspot()
            } catch (exception: ApiException) {
                // Handle cases with location is throwing error
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val resolvable = exception as ResolvableApiException
                        resolvable.startResolutionForResult(
                            activity,
                            Constants.REQUEST_PERMISSION_LOCATION
                        )
                    } catch (e: Exception) {
                        callback.showGotoSettingsMessage()
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> callback.showGotoSettingsMessage()
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun startHotspot() {
        if (!isLocationEnabled) {
            return
        }
        val manager =
            activity.applicationContext.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        manager.startLocalOnlyHotspot(object : WifiManager.LocalOnlyHotspotCallback() {
            override fun onStarted(reservation: WifiManager.LocalOnlyHotspotReservation) {
                super.onStarted(reservation)
                this@HotspotHandler.manager = reservation
                isEnabled = true
                val key = reservation.wifiConfiguration?.preSharedKey
                val ssid = reservation.wifiConfiguration?.SSID
                callback.updateUI(ssid, key)
            }

            override fun onStopped() {
                super.onStopped()
                callback.updateUI(null, null)
                isEnabled = false
            }

            override fun onFailed(reason: Int) {
                super.onFailed(reason)
                callback.updateUI(null, null)
                isEnabled = false
            }
        }, Handler(Looper.getMainLooper()))
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun stopHotspot() {
        if (!isLocationEnabled) {
            return
        }
        if (manager != null) {
            manager?.close()
            callback.updateUI(null, null)
            isEnabled = false
        }
    }

    private val isLocationEnabled: Boolean
        private get() {
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    2
                )
                return false
            }
            return true
        }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun updateHotspot() {
        if (!isEnabled) {
            startHotspot()
        } else {
            stopHotspot()
        }
    }

    interface HotspotHandlerCallbacks {
        fun showGotoSettingsMessage()
        fun updateUI(ssid: String?, key: String?)
    }
}