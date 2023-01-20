package com.aleksmurmur.hairdresser.web.dto

import com.aleksmurmur.hairdresser.schedule.dto.TimetableCreateRequest
import com.aleksmurmur.hairdresser.web.dto.TimetableWorkingSlotForm.Mapper.toCreateRequest
import java.time.DayOfWeek
import java.time.LocalDate

class TimetableCreateForm (
    var dateFrom: LocalDate? = null,
    var dateTo: LocalDate? = null,
    var workingSchedule: Map<DayOfWeek, TimetableWorkingSlotForm> = mapOf()
        ) {
    companion object Mapper {
        fun TimetableCreateForm.toCreateRequest() = TimetableCreateRequest(
            dateFrom!!,
            dateTo!!,
            workingSchedule.mapValues { it.value.toCreateRequest()}
        )
        }
    }
