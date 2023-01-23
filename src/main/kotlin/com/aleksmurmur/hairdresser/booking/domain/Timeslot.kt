package com.aleksmurmur.hairdresser.booking.domain

import com.aleksmurmur.hairdresser.common.jpa.UUIDIdentifiableEntity
import com.aleksmurmur.hairdresser.schedule.domain.DaySchedule
import jakarta.persistence.*
import java.time.Duration
import java.time.LocalTime

@Entity
@Table(name = "timeslots")
@Inheritance(strategy = InheritanceType.JOINED)
class Timeslot (
    var timeFrom: LocalTime,
    var duration: Duration,
//    var timeTo: LocalTime,
    @ManyToOne
    var daySchedule: DaySchedule,
    @Enumerated(EnumType.STRING)
    var timeslotStatus: TimeslotStatus
        ) : UUIDIdentifiableEntity()
