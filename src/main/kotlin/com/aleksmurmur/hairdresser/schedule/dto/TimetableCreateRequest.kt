package com.aleksmurmur.hairdresser.schedule.dto

import java.time.DayOfWeek
import java.time.LocalDate

data class TimetableCreateRequest (
    val dateFrom: LocalDate,
    val dateTo: LocalDate,
    val workingSchedule: Map<DayOfWeek, TimetableWorkingSlot>
        ) {
}