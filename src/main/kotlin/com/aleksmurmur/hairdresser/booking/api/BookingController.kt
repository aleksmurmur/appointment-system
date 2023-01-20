package com.aleksmurmur.hairdresser.booking.api

import com.aleksmurmur.hairdresser.api.BOOKING_PATH
import com.aleksmurmur.hairdresser.booking.dto.BookingCreateRequest
import com.aleksmurmur.hairdresser.booking.dto.BookingResponse
import com.aleksmurmur.hairdresser.booking.dto.BookingTimeAndProductsUpdateRequest
import com.aleksmurmur.hairdresser.booking.service.BookingService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping(BOOKING_PATH)
class BookingController(
    private val bookingService: BookingService
        ){

    @PostMapping
    fun createBooking(@RequestBody @Valid request: BookingCreateRequest): ResponseEntity<BookingResponse> =
        ResponseEntity.ok(bookingService.createBooking(request))

    @PatchMapping("/{id}")
    fun updateBookingTimeAndProducts(
        @PathVariable id: UUID,
        @RequestBody request: BookingTimeAndProductsUpdateRequest
    ): ResponseEntity<BookingResponse> =
        ResponseEntity.ok(bookingService.updateBookingTimeAndProducts(id, request))

    @GetMapping("/{id}")
    fun getBooking(@PathVariable id: UUID): ResponseEntity<BookingResponse> =
        ResponseEntity.ok(bookingService.getById(id))

    @GetMapping("/clients/{id}")
    fun getAllByClient(@PathVariable id: UUID) : ResponseEntity<List<BookingResponse>> =
        bookingService.getAllByClient(id)
            .let {
                ResponseEntity.ok(it)
            }

    @DeleteMapping("/{id}")
    fun cancelBooking(@PathVariable id: UUID) : ResponseEntity<Unit> =
        bookingService.cancelBooking(id)
            .let { ResponseEntity.noContent().build() }


}