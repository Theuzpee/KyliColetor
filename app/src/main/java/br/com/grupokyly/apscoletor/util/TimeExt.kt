package br.com.grupokyly.apscoletor.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toTimeString(): String {
    val formatter = SimpleDateFormat("HH:mm", Locale("pt", "BR"))
    return formatter.format(Date(this))
}
