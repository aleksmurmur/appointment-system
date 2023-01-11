package com.aleksmurmur.hairdresser.booking.repository

import com.aleksmurmur.hairdresser.booking.domain.Booking
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface BookingRepository : JpaRepository<Booking, UUID> {

    fun findByClient(id: UUID) : List<Booking>
}