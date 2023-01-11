package com.aleksmurmur.hairdresser.schedule.domain

import com.aleksmurmur.hairdresser.booking.domain.Timeslot
import com.aleksmurmur.hairdresser.common.jpa.DateIdentifiableEntity
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import java.time.LocalDate
import java.time.LocalTime

@Entity
class DaySchedule (
        date: LocalDate,
        var workingTimeFrom: LocalTime?,
        var workingTimeTo: LocalTime?,
        var workingDay: Boolean,
        @OneToMany (mappedBy = "daySchedule")
        var bookedTimeslots: MutableList<Timeslot> = mutableListOf()
        ) : DateIdentifiableEntity(date) {
}