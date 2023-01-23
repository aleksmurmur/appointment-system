package com.aleksmurmur.hairdresser.booking.repository

import com.aleksmurmur.hairdresser.booking.domain.Booking
import com.aleksmurmur.hairdresser.booking.domain.BookingStatus
import com.aleksmurmur.hairdresser.client.domain.Client
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface BookingRepository : JpaRepository<Booking, UUID> {

    fun findAllByBookingStatusEquals(status: BookingStatus = BookingStatus.BOOKED) : List<Booking>
    fun findByClientAndBookingStatusEquals(client: Client, status: BookingStatus) : List<Booking>
    fun findByClient(client: Client) : List<Booking>

    @Query ("""SELECT b FROM Booking b 
        INNER JOIN Timeslot t ON b.id = t.id
        WHERE b.bookingStatus = 'BOOKED'
        AND t.daySchedule.id = CURRENT_DATE 
        AND t.timeFrom < CURRENT_TIME
    """)
    fun findBookingsToComplete() : List<Booking>
}