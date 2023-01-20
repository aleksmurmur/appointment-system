package com.aleksmurmur.hairdresser.web.dto

import com.aleksmurmur.hairdresser.schedule.dto.DayScheduleCreateOrUpdateRequest
import com.aleksmurmur.hairdresser.schedule.dto.DayScheduleResponse
import jakarta.validation.constraints.FutureOrPresent
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalTime

class DayScheduleCreateOrUpdateForm (
    @field:[FutureOrPresent NotNull]
    var date: LocalDate? = null,
    var timeFrom: LocalTime? = null,
    var timeTo: LocalTime? = null,
    var workingDay: Boolean = true
        ){

    companion object Mapper {
        fun DayScheduleCreateOrUpdateForm.toCreateRequest() = DayScheduleCreateOrUpdateRequest(
            date!!,
            timeFrom,
            timeTo,
            workingDay
        )

        fun DayScheduleResponse.toForm() = DayScheduleCreateOrUpdateForm(
            date,
            timeFrom,
            timeTo,
            workingDay
        )
    }
}