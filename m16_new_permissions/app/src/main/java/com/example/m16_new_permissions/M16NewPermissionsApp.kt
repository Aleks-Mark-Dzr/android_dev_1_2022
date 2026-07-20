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
        osmConfig.userAgentValue = buildUserAgent()

        val cacheRoot = File(cacheDir, "osmdroid")
        osmConfig.osmdroidBasePath = cacheRoot
        osmConfig.osmdroidTileCache = File(cacheRoot, "tiles")
    }

    private fun buildUserAgent(): String {
        return "$packageName/${BuildConfig.VERSION_NAME} (Android; osmdroid; contact: example@example.com)"
    }
}