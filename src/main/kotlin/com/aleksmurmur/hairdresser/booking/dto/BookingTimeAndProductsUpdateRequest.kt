package com.aleksmurmur.hairdresser.booking.dto

import java.time.LocalTime
import java.util.UUID

data class BookingTimeAndProductsUpdateRequest (
    val timeFrom: LocalTime,
    val products: List<UUID>
        ) {
}