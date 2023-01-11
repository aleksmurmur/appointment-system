package com.aleksmurmur.hairdresser.booking.dto

import com.aleksmurmur.hairdresser.booking.domain.BookingStatus
import com.aleksmurmur.hairdresser.product.dto.ProductResponse
import com.aleksmurmur.hairdresser.booking.domain.Booking
import java.time.LocalDateTime
import java.util.UUID

class BookingResponse (
    val id: UUID,
    val time: LocalDateTime,
    val status: BookingStatus,
    val products: List<ProductResponse>
    ){
    object Mapper {
        fun from(booking: Booking) = BookingResponse(
            id = booking.persistentId,
            time = booking.time.let { LocalDateTime.of(it.daySchedule.persistentId, it.timeFrom) },
            status = booking.status,
            products = booking.products.map { ProductResponse.Mapper.from(it) }
        )
    }

}