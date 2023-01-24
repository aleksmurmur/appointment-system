package com.aleksmurmur.hairdresser.web.dto

import com.aleksmurmur.hairdresser.booking.dto.BookingCreateRequest
import jakarta.validation.constraints.FutureOrPresent
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

class BookingCreateForm(
    @field:FutureOrPresent @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val date: LocalDate? = null,
    @field:NotNull
    val timeFrom: LocalTime? = null,
    @field:NotNull
    val clientId: UUID? = null,
    @field:NotEmpty
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