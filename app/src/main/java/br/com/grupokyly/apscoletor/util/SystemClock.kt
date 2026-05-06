package br.com.grupokyly.apscoletor.util

import javax.inject.Inject

class SystemClock @Inject constructor() : Clock {
    override fun now(): Long = System.currentTimeMillis()
}
