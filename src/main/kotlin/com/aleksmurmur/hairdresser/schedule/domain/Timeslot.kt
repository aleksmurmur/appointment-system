package com.aleksmurmur.hairdresser.schedule.domain

import com.aleksmurmur.hairdresser.common.jpa.UUIDIdentifiableEntity
import jakarta.persistence.Entity
import jakarta.persistence.ManyToOne
import java.time.LocalTime

@Entity
class Timeslot (
    var timeFrom: LocalTime,
    var timeTo: LocalTime,
    @ManyToOne
    var daySchedule: DaySchedule,
    var status: TimeslotStatus
        ) : UUIDIdentifiableEntity()
