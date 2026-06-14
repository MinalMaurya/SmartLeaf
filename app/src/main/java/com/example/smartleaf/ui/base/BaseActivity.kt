package com.example.smartleaf.ui.base

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.example.smartleaf.ui.utils.LocaleHelper

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val context = LocaleHelper.onAttach(newBase)
        super.attachBaseContext(context)
    }
}
