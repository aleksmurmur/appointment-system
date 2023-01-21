package com.aleksmurmur.hairdresser.web.dto

import com.aleksmurmur.hairdresser.booking.dto.BookingCreateRequest
import jakarta.validation.constraints.FutureOrPresent
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

class BookingCreateForm(
    @field:FutureOrPresent @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val date: LocalDate? = null,
    val timeFrom: LocalTime? = null,
    val clientId: UUID? = null,
    val products: List<UUID> = listOf()
) {
    companion object Mapper {
        fun BookingCreateForm.toCreateRequest() = BookingCreateRequest(
            date!!,
            timeFrom!!,
            clientId!!,
            products
        )
    }
}