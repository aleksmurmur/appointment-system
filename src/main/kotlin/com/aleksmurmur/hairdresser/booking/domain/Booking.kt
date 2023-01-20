package com.aleksmurmur.hairdresser.booking.domain

import com.aleksmurmur.hairdresser.client.domain.Client
import com.aleksmurmur.hairdresser.product.domain.Product
import com.aleksmurmur.hairdresser.schedule.domain.DaySchedule
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Duration
import java.time.LocalTime

@Entity
@Table(name = "bookings")
class Booking(
     timeFrom: LocalTime,
     duration: Duration,
//    @ManyToOne
     daySchedule: DaySchedule,
     timeslotStatus : TimeslotStatus = TimeslotStatus.BUSY,
    var bookingStatus: BookingStatus = BookingStatus.BOOKED,
    @ManyToOne
    var client: Client,
    @ManyToMany
    @JoinTable(
        name = "booking_products",
        joinColumns = [JoinColumn(name = "booking_id")],
        inverseJoinColumns = [JoinColumn(name = "product_id")]
    )
        var products: MutableList<Product> = mutableListOf()
) : Timeslot(
    timeFrom = timeFrom,
    duration = duration,
    daySchedule = daySchedule,
    timeslotStatus = timeslotStatus
) {
}