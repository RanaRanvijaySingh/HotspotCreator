package com.simple.hotspotcreator

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.net.wifi.WifiManager.LocalOnlyHotspotReservation
import android.os.Bundle
import android.os.Build
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import androidx.annotation.RequiresApi
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.LocalOnlyHotspotCallback
import android.os.Looper
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import android.widget.Toast
import android.content.Intent
import android.os.Handler
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.simple.hotspotcreator.databinding.ActivityMainBinding
import java.lang.Exception
import javax.inject.Inject

const val REQUEST_PERMISSION_LOCATION = 111

class HomeActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: HomeViewModel
    private lateinit var binding: ActivityMainBinding
    private var manager: LocalOnlyHotspotReservation? = null
    private var isEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        (application as MyApplication).appComponent.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[HomeViewModel::class.java]
        updateUI(R.string.start_hotspot_messagee, "", "")
        binding.buttonCreateHotspot.setOnClickListener { view: View? ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                enableLocationSettings()
            }
        }
    }

    /**
     * Function to initialize all the observers on [LiveData]
     */
    private fun initObservers() {
        viewModel.isLoadingLiveData.observe(this, {
//            updateProgress(it)
        })
    }

    private val isLocationEnabled: Boolean
        private get() {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    2
                )
                return false
            }
            return true
        }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun startHotspot() {
        if (!isLocationEnabled) {
            return
        }
        val manager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        manager.startLocalOnlyHotspot(object : LocalOnlyHotspotCallback() {
            override fun onStarted(reservation: LocalOnlyHotspotReservation) {
                super.onStarted(reservation)
                this@HomeActivity.manager = reservation
                isEnabled = true
                val key = reservation.wifiConfiguration?.preSharedKey
                val ssid = reservation.wifiConfiguration?.SSID
                updateUI(R.string.hotspot_created_message, ssid, key)
            }

            override fun onStopped() {
                super.onStopped()
                updateUI(R.string.start_hotspot_messagee, "", "")
                isEnabled = false
            }

            override fun onFailed(reason: Int) {
                super.onFailed(reason)
                updateUI(R.string.hotspot_error_message, "", "")
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
            updateUI(R.string.start_hotspot_messagee, "", "")
            isEnabled = false
        }
    }

    private fun updateUI(stringId: Int, ssid: String?, key: String?) {
        binding.apply {
            if (ssid.isNullOrEmpty() || key.isNullOrEmpty()) {
                buttonCreateHotspot.text = resources.getString(R.string.start_hotspot)
            } else {
                buttonCreateHotspot.text = resources.getString(R.string.stop_hotspot)
            }
            tvMessage.text = resources.getString(stringId)
            tvSSID.text = String.format(resources.getString(R.string.ssid), ssid)
            tvKey.text = String.format(resources.getString(R.string.key), key)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun updateHotspot() {
        if (!isEnabled) {
            startHotspot()
        } else {
            stopHotspot()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun enableLocationSettings() {
        val locationRequest = LocationRequest()
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest).setAlwaysShow(false)
        val task = LocationServices.getSettingsClient(this)
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
                            this@HomeActivity,
                            REQUEST_PERMISSION_LOCATION
                        )
                    } catch (e: Exception) {
                        showGotoSettingsMessage()
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> showGotoSettingsMessage()
                }
            }
        }
    }

    private fun showGotoSettingsMessage() {
        Toast.makeText(
            this@HomeActivity,
            resources.getString(R.string.goto_settings), Toast.LENGTH_SHORT
        ).show()
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val states = LocationSettingsStates.fromIntent(data)
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            when (resultCode) {
                RESULT_OK -> updateHotspot()
                RESULT_CANCELED -> showRequestCancelledMessage()
                else -> {}
            }
        }
    }

    private fun showRequestCancelledMessage() {
        Toast.makeText(
            this@HomeActivity,
            resources.getString(R.string.cancelled), Toast.LENGTH_SHORT
        ).show()
    }
}