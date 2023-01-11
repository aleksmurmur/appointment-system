package com.aleksmurmur.hairdresser.booking.dto

import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class BookingCreateRequest (
    val date: LocalDate,
    val timeFrom: LocalTime,
    val clientId: UUID,
    val products: List<UUID>
        ) {
}