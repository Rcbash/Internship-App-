package com.example.nammaraste.utils

import java.util.Calendar
import java.util.concurrent.atomic.AtomicInteger

object TicketIdGenerator {

    private val counter = AtomicInteger(0)

    /**
     * Generates a unique Ticket ID in the format NR-YYYY-NNNNN
     * Example: NR-2026-00125
     */
    fun generate(): String {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val number = (System.currentTimeMillis() % 100000).toInt()
        return String.format("%s-%d-%05d", Constants.TICKET_PREFIX, year, number)
    }
}