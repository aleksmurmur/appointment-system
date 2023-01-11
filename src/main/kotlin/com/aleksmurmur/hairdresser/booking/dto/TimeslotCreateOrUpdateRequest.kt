package com.aleksmurmur.hairdresser.booking.dto

import com.aleksmurmur.hairdresser.booking.domain.TimeslotStatus
import jakarta.validation.constraints.FutureOrPresent
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

data class TimeslotCreateOrUpdateRequest(
    @field:FutureOrPresent
    val date: LocalDate,
    val timeFrom: LocalTime,
    val duration: Duration,
    val status: TimeslotStatus
) {
}