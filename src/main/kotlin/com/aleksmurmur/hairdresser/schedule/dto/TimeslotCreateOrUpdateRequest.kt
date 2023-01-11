package com.aleksmurmur.hairdresser.schedule.dto

import com.aleksmurmur.hairdresser.schedule.domain.TimeslotStatus
import java.time.LocalDate
import java.time.LocalTime

data class TimeslotCreateOrUpdateRequest(
    val timeFrom: LocalTime,
    val timeTo: LocalTime,
    val dayScheduleId: LocalDate,
    val status: TimeslotStatus
) {
}