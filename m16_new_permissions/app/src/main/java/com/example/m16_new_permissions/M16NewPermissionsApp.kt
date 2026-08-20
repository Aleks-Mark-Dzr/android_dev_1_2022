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

        val cacheRoot = File(cacheDir, "osmdroid/$OSM_CACHE_FOLDER")
        osmConfig.osmdroidBasePath = cacheRoot
        osmConfig.osmdroidTileCache = File(cacheRoot, "tiles")
    }

    companion object {
        private const val OSM_CACHE_FOLDER = "osm-tiles-v2"

        /**
         * OpenStreetMap blocks requests with generic/default clients: the osmdroid default
         * ("osmdroid") and template package ids (com.example.*) are rejected with the
         * "Access blocked" tiles. The User-Agent must identify this app uniquely.
         * TODO: добавить контактный e-mail в скобках, как рекомендует политика OSM.
         */
        private val OSM_USER_AGENT =
            "SkillboxAttractionsMap/${BuildConfig.VERSION_NAME} (Android)"
    }
}