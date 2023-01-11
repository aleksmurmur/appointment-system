package com.aleksmurmur.hairdresser.booking.dto

import com.aleksmurmur.hairdresser.booking.domain.BookingStatus
import com.aleksmurmur.hairdresser.product.dto.ProductResponse
import com.aleksmurmur.hairdresser.booking.domain.Booking
import com.aleksmurmur.hairdresser.client.dto.ClientResponse
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class BookingResponse (
    val id: UUID,
    val timeFrom: LocalTime,
    val timeTo: LocalTime,
    val date: LocalDate,
    val status: BookingStatus,
    val client: ClientResponse,
    val products: List<ProductResponse>
    ){
    object Mapper {
        fun from(booking: Booking) = BookingResponse(
            id = booking.persistentId,
            timeFrom = booking.timeFrom,
            timeTo = booking.timeFrom.plus(booking.duration),
            date = booking.daySchedule.persistentId,
            status = booking.bookingStatus,
            client = booking.client.let { ClientResponse.Mapper.from(it) },
            products = booking.products.map { ProductResponse.Mapper.from(it) }
        )
    }

}