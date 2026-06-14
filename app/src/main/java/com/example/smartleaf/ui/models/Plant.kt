package com.example.smartleaf.ui.models

import androidx.annotation.DrawableRes

data class Plant(
    val id: String,
    val name: String,
    val description: String,
    @DrawableRes val healthyImageResId: Int,
    @DrawableRes val diseasedImageResId: Int,
    val diseaseInfo: String
)