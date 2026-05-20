package br.com.grupokyly.apscoletor.domain.hardware

interface FeedbackProvider {
    fun scanPartialSuccess()
    fun scanSkuComplete()
    fun scanError()
    fun boxFinal()
    fun boxPartial()
    fun scanSequenceError()
    fun scanDuplicateError()
    fun scanDivergenceSaved()
    fun release()
}
