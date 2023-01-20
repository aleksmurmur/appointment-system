package com.aleksmurmur.hairdresser.schedule.dto


import com.aleksmurmur.hairdresser.booking.dto.TimeslotResponse
import java.time.LocalDate
import java.time.LocalTime

class DayScheduleResponse (
    val date: LocalDate,
    val timeFrom: LocalTime?,
    val timeTo: LocalTime?,
    var timeslots: MutableList<TimeslotResponse> = mutableListOf(),
    val workingDay: Boolean

    ) {

}