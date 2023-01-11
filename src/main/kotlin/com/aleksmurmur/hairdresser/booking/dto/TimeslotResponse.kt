package com.aleksmurmur.hairdresser.booking.dto

import com.aleksmurmur.hairdresser.booking.domain.Timeslot
import com.aleksmurmur.hairdresser.booking.domain.TimeslotStatus
import java.time.LocalTime

class TimeslotResponse (
    val timeFrom: LocalTime,
    val timeTo: LocalTime,
    val status: TimeslotStatus
        ){
    object Mapper {
        fun from(t : Timeslot) = TimeslotResponse(
            t.timeFrom,
            t.timeFrom.plus(t.duration),
            t.timeslotStatus
        )
    }
}