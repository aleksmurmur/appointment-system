package com.aleksmurmur.hairdresser.booking.service

import com.aleksmurmur.hairdresser.booking.dto.BookingResponse
import com.aleksmurmur.hairdresser.booking.repository.BookingRepository
import com.aleksmurmur.hairdresser.client.service.ClientService
import com.aleksmurmur.hairdresser.common.jpa.findByIdOrThrow
import jakarta.annotation.security.RolesAllowed
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class BookingService (
    val bookingRepository: BookingRepository,
    val clientService: ClientService
        ){

    @Transactional(readOnly = true)
    @RolesAllowed("admin.bookings:read")
    fun getById(id: UUID) : BookingResponse =
        bookingRepository.findByIdOrThrow(id)
            .let { BookingResponse.Mapper.from(it) }

    @Transactional(readOnly = true)
    @RolesAllowed("admin.bookings:read")
    fun getAllByClient(clientId: UUID) : List<BookingResponse> =
        bookingRepository.findByClient(clientId)
            .map { BookingResponse.Mapper.from(it) }

//    @Transactional
//    @RolesAllowed("admin.bookings:write")
//    fun createBooking(request: )

}