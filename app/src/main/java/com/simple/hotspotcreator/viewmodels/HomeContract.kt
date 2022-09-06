package com.simple.hotspotcreator.viewmodels

import com.simple.hotspotcreator.utils.HotspotHandler

interface HomeContract {
    fun handleHotspotButtonClick(hotspotHandler: HotspotHandler)
    fun updateHotspotStatus(hotspotHandler: HotspotHandler)
    fun handleCancelPermission()
}