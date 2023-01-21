package com.aleksmurmur.hairdresser.web.dto

import com.aleksmurmur.hairdresser.booking.dto.BookingResponse
import com.aleksmurmur.hairdresser.booking.dto.BookingTimeAndProductsUpdateRequest
import java.time.LocalTime
import java.util.UUID

class BookingTimeAndProductsUpdateForm (
    var timeFrom: LocalTime,
    var products: List<UUID>
        ) {
    companion object Mapper {
        fun BookingResponse.toForm() = BookingTimeAndProductsUpdateForm(
            timeFrom,
            products.map { it.id }
        )

        fun BookingTimeAndProductsUpdateForm.toUpdateRequest() = BookingTimeAndProductsUpdateRequest(
            timeFrom,
            products
        )
    }
}