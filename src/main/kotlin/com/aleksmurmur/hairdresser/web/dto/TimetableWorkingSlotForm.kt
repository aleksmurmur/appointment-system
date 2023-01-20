package com.aleksmurmur.hairdresser.web.dto

import com.aleksmurmur.hairdresser.schedule.dto.TimetableWorkingSlot
import java.time.LocalTime

class TimetableWorkingSlotForm(
    var timeFrom: LocalTime? = null,
    var timeTo: LocalTime? = null,
    var workingDay: Boolean = true
) {
    companion object Mapper {
        fun TimetableWorkingSlotForm.toCreateRequest() = TimetableWorkingSlot(
            timeFrom,
            timeTo,
            workingDay
        )
    }
}