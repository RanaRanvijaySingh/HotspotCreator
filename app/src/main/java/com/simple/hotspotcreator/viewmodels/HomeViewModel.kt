package com.simple.hotspotcreator.viewmodels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simple.hotspotcreator.utils.Constants
import com.simple.hotspotcreator.utils.HotspotHandler
import javax.inject.Inject

class HomeViewModel @Inject constructor() : ViewModel(), HomeContract,
    HotspotHandler.HotspotHandlerCallbacks {
    val bCreateHotspotData = MutableLiveData<String>()
    val tvMessageData = MutableLiveData<String>()
    val tvSSIDData = MutableLiveData<String>()
    val tvKey = MutableLiveData<String>()
    val toastMessageData = MutableLiveData<String>()

    override fun handleHotspotButtonClick(hotspotHandler: HotspotHandler) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            hotspotHandler.setHotspotHandlerCallback(this)
            hotspotHandler.enableLocationSettings()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun updateHotspotStatus(hotspotHandler: HotspotHandler) {
        hotspotHandler.updateHotspot()
    }

    override fun showGotoSettingsMessage() {
        toastMessageData.value = Constants.GOTO_SETTINGS
    }

    private fun setStartHotspotContent() {
        bCreateHotspotData.value = Constants.START_HOTSPOT
        tvSSIDData.value = Constants.SSID
        tvKey.value = Constants.KEY
        tvMessageData.value = Constants.START_HOTSPOT_MESSAGE
    }

    private fun setStopHotspotContent(ssid: String, key: String) {
        bCreateHotspotData.value = Constants.STOP_HOTSPOT
        tvSSIDData.value = Constants.SSID + ssid
        tvKey.value = Constants.KEY + key
        tvMessageData.value = Constants.HOTSPOT_CREATED_MESSAGE
    }

    override fun updateUI(ssid: String?, key: String?) {
        if (ssid.isNullOrEmpty() || key.isNullOrEmpty()) {
            setStartHotspotContent()
        } else {
            setStopHotspotContent(ssid, key)
        }
    }

    override fun handleCancelPermission() {
        toastMessageData.value = Constants.GOTO_SETTINGS
    }
}