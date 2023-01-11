package com.aleksmurmur.hairdresser.schedule.dto

import com.aleksmurmur.hairdresser.schedule.domain.Timeslot
import com.aleksmurmur.hairdresser.schedule.domain.TimeslotStatus
import java.time.LocalTime

class TimeslotResponse (
    val timeFrom: LocalTime,
    val timeTo: LocalTime,
    val status: TimeslotStatus
        ){
    object Mapper {
        fun from(t : Timeslot) = TimeslotResponse(
            t.timeFrom,
            t.timeTo,
            t.status
        )
    }
}