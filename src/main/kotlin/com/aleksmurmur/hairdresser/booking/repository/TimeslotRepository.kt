package com.aleksmurmur.hairdresser.booking.repository

import com.aleksmurmur.hairdresser.booking.domain.Timeslot
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
interface TimeslotRepository : JpaRepository<Timeslot, UUID> {

    @Query("""
        SELECT ts FROM Timeslot ts
        WHERE ts.daySchedule.id = :date
    """)
    fun findAllByDaySchedule(date: LocalDate) : List<Timeslot>
}