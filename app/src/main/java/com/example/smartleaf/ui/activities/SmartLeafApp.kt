package com.example.smartleaf.ui.activities


import android.app.Application
import android.content.Context
import com.example.smartleaf.ui.utils.LocaleHelper

class SmartLeafApp : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(base))
    }
}

