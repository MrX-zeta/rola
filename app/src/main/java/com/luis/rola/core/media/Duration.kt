package com.luis.rola.core.media

/** «m:ss», o «h:mm:ss» desde una hora. */
fun formatDuration(ms: Long): String {
    val s = ms.coerceAtLeast(0) / 1000
    val h = s / 3600
    val mm = s % 3600 / 60
    val ss = (s % 60).toString().padStart(2, '0')
    return if (h > 0) "$h:${mm.toString().padStart(2, '0')}:$ss" else "$mm:$ss"
}
