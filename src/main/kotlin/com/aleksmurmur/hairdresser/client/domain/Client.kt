package com.aleksmurmur.hairdresser.client.domain

import com.aleksmurmur.hairdresser.common.jpa.UUIDIdentifiableEntity
import com.aleksmurmur.hairdresser.booking.domain.Booking
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "clients")
class Client (
    var phone: String,
    var name: String? = null,
    @OneToMany (fetch = FetchType.LAZY, mappedBy = "client")
    var bookings: MutableList<Booking> = mutableListOf(),
    var deleted: Boolean = false
        ): UUIDIdentifiableEntity()