package br.com.grupokyly.apscoletor.test.fake

import br.com.grupokyly.apscoletor.util.Clock

class FakeClock(private var currentTime: Long = 0L) : Clock {
    override fun now(): Long = currentTime
    fun advanceTo(time: Long) { currentTime = time }
}
