package com.aleksmurmur.hairdresser.booking.dto

import jakarta.validation.constraints.FutureOrPresent
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class BookingCreateRequest (
    @field:FutureOrPresent @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val date: LocalDate,
    val timeFrom: LocalTime,
    val clientId: UUID,
    val products: List<UUID>
        ) {
}