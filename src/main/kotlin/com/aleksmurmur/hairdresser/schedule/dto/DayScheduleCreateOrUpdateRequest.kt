package com.aleksmurmur.hairdresser.schedule.dto

import jakarta.validation.constraints.FutureOrPresent
import java.time.LocalDate
import java.time.LocalTime

data class DayScheduleCreateOrUpdateRequest (
        @field:FutureOrPresent
        val date: LocalDate,
        val timeFrom: LocalTime?,
        val timeTo: LocalTime?,
        val workingDay: Boolean
        ){
}