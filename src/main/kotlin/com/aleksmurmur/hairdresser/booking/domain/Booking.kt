package com.aleksmurmur.hairdresser.booking.domain

import com.aleksmurmur.hairdresser.client.domain.Client
import com.aleksmurmur.hairdresser.common.jpa.UUIDIdentifiableEntity
import com.aleksmurmur.hairdresser.product.domain.Product
import com.aleksmurmur.hairdresser.schedule.domain.Timeslot
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "bookings")
class Booking(
    @OneToOne
    var time: Timeslot,
    var status: BookingStatus = BookingStatus.BOOKED,
    @ManyToOne
    var client: Client,
    @ManyToMany
    @JoinTable(
        name = "booking_products",
        joinColumns = [JoinColumn(name = "booking_id")],
        inverseJoinColumns = [JoinColumn(name = "product_id")]
    )
        var products: MutableList<Product> = mutableListOf()
) : UUIDIdentifiableEntity() {
}