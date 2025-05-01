package com.furia.furiafanapp.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val dateFormat = SimpleDateFormat("dd/MM • HH:mm", Locale.getDefault())

    fun formatMatchDate(date: Date): String {
        return dateFormat.format(date)
    }

    fun isToday(date: Date): Boolean {
        val calendar1 = Calendar.getInstance()
        calendar1.time = date

        val calendar2 = Calendar.getInstance()
        calendar2.time = Date()

        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
                calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
    }
} 