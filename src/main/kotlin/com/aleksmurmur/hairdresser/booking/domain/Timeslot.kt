package com.aleksmurmur.hairdresser.booking.domain

import com.aleksmurmur.hairdresser.common.jpa.UUIDIdentifiableEntity
import com.aleksmurmur.hairdresser.schedule.domain.DaySchedule
import jakarta.persistence.Entity
import jakarta.persistence.ManyToOne
import java.time.Duration
import java.time.LocalTime

@Entity
class Timeslot (
    var timeFrom: LocalTime,
    var duration: Duration,
//    var timeTo: LocalTime,
    @ManyToOne
    var daySchedule: DaySchedule,
    var timeslotStatus: TimeslotStatus
        ) : UUIDIdentifiableEntity()
