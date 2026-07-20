package com.example.m16_new_permissions

import android.app.Application
import org.osmdroid.config.Configuration
import java.io.File

class M16NewPermissionsApp : Application() {

    override fun onCreate() {
        super.onCreate()
        configureOsmDroid()
    }

    private fun configureOsmDroid() {
        val osmConfig = Configuration.getInstance()
        osmConfig.userAgentValue = OSM_USER_AGENT

        val cacheRoot = File(cacheDir, "osmdroid/\$OSM_CACHE_FOLDER")
        osmConfig.osmdroidBasePath = cacheRoot
        osmConfig.osmdroidTileCache = File(cacheRoot, "tiles")
    }

    companion object {
        private const val OSM_CACHE_FOLDER = "osm-tiles-v2"

        /**
         * OpenStreetMap blocks requests with generic/default clients. Use a stable,
         * app-specific User-Agent so osmdroid tile requests comply with OSM policy.
         */
        private val OSM_USER_AGENT = listOf(
            BuildConfig.APPLICATION_ID,
            BuildConfig.VERSION_NAME,
            "Android",
            "osmdroid"
        ).joinToString(separator = "; ")
    }
}