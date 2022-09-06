package com.simple.hotspotcreator.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Build
import androidx.annotation.RequiresApi
import android.widget.Toast
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.*
import com.simple.hotspotcreator.MyApplication
import com.simple.hotspotcreator.databinding.ActivityMainBinding
import com.simple.hotspotcreator.utils.Constants
import com.simple.hotspotcreator.utils.HotspotHandler
import com.simple.hotspotcreator.viewmodels.HomeViewModel
import javax.inject.Inject

class HomeActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: HomeViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var hotspotHandler: HotspotHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Init binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Init view model
        (application as MyApplication).appComponent.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[HomeViewModel::class.java]
        // Create hotspot handler to take care of heavy operations
        hotspotHandler = HotspotHandler(this)
        // Init UI components
        binding.bCreateHotspot.setOnClickListener {
            viewModel.handleHotspotButtonClick(hotspotHandler)
        }
        // Init observers
        initObservers()
    }

    /**
     * Function to initialize all the observers on [LiveData]
     */
    private fun initObservers() {
        viewModel.bCreateHotspotData.observe(this, { updateButton(it) })
        viewModel.tvSSIDData.observe(this, { updateTvSSID(it) })
        viewModel.tvKey.observe(this, { updateTvKey(it) })
        viewModel.tvMessageData.observe(this, { updateTvMessage(it) })
        viewModel.toastMessageData.observe(this, { showToastMessage(it) })
    }

    private fun showToastMessage(message: String?) =
        message?.let { Toast.makeText(this, message, Toast.LENGTH_LONG).show() }

    private fun updateTvMessage(text: String?) = text?.let { binding.tvMessage.text = it }

    private fun updateTvKey(text: String?) = text?.let { binding.tvKey.text = it }

    private fun updateTvSSID(text: String?) = text?.let { binding.tvSSID.text = it }

    private fun updateButton(text: String?) = text?.let { binding.bCreateHotspot.text = it }

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val states = LocationSettingsStates.fromIntent(data)
        if (requestCode == Constants.REQUEST_PERMISSION_LOCATION) {
            when (resultCode) {
                RESULT_OK -> viewModel.updateHotspotStatus(hotspotHandler)
                RESULT_CANCELED -> viewModel.handleCancelPermission()
                else -> {}
            }
        }
    }
}