package com.example.billbudddy

import android.app.Application
import com.cloudinary.android.MediaManager
import java.util.HashMap

class BillBuddyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initCloudinary()
    }

    private fun initCloudinary() {
        try {
            val config = HashMap<String, String>().apply {
                put("cloud_name", "de1qpq9sy")
                put("api_key", "124981474234925")
                put("api_secret", "fOyn6ZPE0fNlDnhZq_dS9aaQrws")
                put("secure", "true")
                put("upload_preset", "chat_image")  // Updated to use your existing preset
            }
            MediaManager.init(this, config)
        } catch (e: IllegalStateException) {
            // Already initialized
        }
    }
} 