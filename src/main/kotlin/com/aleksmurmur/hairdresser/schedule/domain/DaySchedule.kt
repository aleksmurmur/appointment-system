package com.aleksmurmur.hairdresser.schedule.domain

import com.aleksmurmur.hairdresser.common.jpa.DateIdentifiableEntity
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import java.time.LocalDate
import java.time.LocalTime

@Entity
class DaySchedule (
        //FIXME convert to zoneddatetime?
        date: LocalDate,
        var workingTimeFrom: LocalTime?,
        var workingTimeTo: LocalTime?,
        var workingDay: Boolean, //FIXME add to service
        @OneToMany (mappedBy = "daySchedule")
        var bookedTimeslots: MutableList<Timeslot> = mutableListOf()
        ) : DateIdentifiableEntity(date) {
}