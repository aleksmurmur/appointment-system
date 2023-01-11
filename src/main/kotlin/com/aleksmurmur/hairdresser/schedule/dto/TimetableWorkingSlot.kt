package com.aleksmurmur.hairdresser.schedule.dto

import java.time.LocalTime

class TimetableWorkingSlot (
    val timeFrom: LocalTime?,
    val timeTo: LocalTime?,
    val workingDay: Boolean
        ) {
}