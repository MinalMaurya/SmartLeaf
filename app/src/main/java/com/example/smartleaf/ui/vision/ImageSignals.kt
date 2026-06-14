package com.example.smartleaf.ui.vision

data class ImageSignals(
    val yellowPct: Int = 0,        // % of yellowing detected
    val brownPct: Int = 0,         // % of browning/necrosis detected
    val whiteLowSatPct: Int = 0    // % of whitish powdery areas detected
)