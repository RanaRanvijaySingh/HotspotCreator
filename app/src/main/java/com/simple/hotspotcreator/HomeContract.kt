package com.simple.hotspotcreator

interface HomeContract {
    fun handleHotspotButtonClick(hotspotHandler: HotspotHandler)
    fun updateHotspotStatus(hotspotHandler: HotspotHandler)
    fun handleCancelPermission()
}