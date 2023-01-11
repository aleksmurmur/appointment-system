package com.aleksmurmur.hairdresser.schedule.dto


import java.time.LocalDate
import java.time.LocalTime

class DayScheduleResponse (
    val date: LocalDate,
    val workingTimeFrom: LocalTime?,
    val workingTimeTo: LocalTime?,
    var timeslots: MutableList<TimeslotResponse> = mutableListOf(),
    val workingDay: Boolean

    ) {

}