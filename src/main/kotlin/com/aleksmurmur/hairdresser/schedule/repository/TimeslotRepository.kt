package com.aleksmurmur.hairdresser.schedule.repository

import com.aleksmurmur.hairdresser.schedule.domain.Timeslot
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TimeslotRepository : JpaRepository<Timeslot, UUID> {
}