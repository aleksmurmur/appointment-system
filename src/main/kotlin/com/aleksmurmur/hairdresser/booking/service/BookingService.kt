package com.aleksmurmur.hairdresser.booking.service

import com.aleksmurmur.hairdresser.booking.domain.Booking
import com.aleksmurmur.hairdresser.booking.domain.BookingStatus
import com.aleksmurmur.hairdresser.booking.dto.BookingCreateRequest
import com.aleksmurmur.hairdresser.booking.dto.BookingResponse
import com.aleksmurmur.hairdresser.booking.dto.BookingTimeAndProductsUpdateRequest
import com.aleksmurmur.hairdresser.booking.repository.BookingRepository
import com.aleksmurmur.hairdresser.client.repository.ClientRepository
import com.aleksmurmur.hairdresser.common.jpa.findByIdOrThrow
import com.aleksmurmur.hairdresser.exception.EntityNotFoundException
import com.aleksmurmur.hairdresser.exception.UnavailableActionException
import com.aleksmurmur.hairdresser.product.domain.Product
import com.aleksmurmur.hairdresser.product.repository.ProductRepository
import com.aleksmurmur.hairdresser.schedule.domain.DaySchedule
import com.aleksmurmur.hairdresser.booking.domain.TimeslotStatus
import com.aleksmurmur.hairdresser.schedule.repository.ScheduleRepository
import com.aleksmurmur.hairdresser.booking.repository.TimeslotRepository
import jakarta.annotation.security.RolesAllowed
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalTime
import java.util.UUID

@Service
class BookingService(
    val bookingRepository: BookingRepository,
    val clientRepository: ClientRepository,
    val timeslotRepository: TimeslotRepository,
    val productRepository: ProductRepository,
    val scheduleRepository: ScheduleRepository
) {

    @Transactional(readOnly = true)
    @RolesAllowed("admin.bookings:read")
    fun getById(id: UUID): BookingResponse =
        bookingRepository.findByIdOrThrow(id)
            .let { BookingResponse.Mapper.from(it) }

    @Transactional(readOnly = true)
    @RolesAllowed("admin.bookings:read")
    fun getAllByClient(clientId: UUID): List<BookingResponse> =
        bookingRepository.findByClient(clientId)
            .map { BookingResponse.Mapper.from(it) }

    @Transactional
    @RolesAllowed("admin.bookings:write")
    fun createBooking(request: BookingCreateRequest): BookingResponse {
        val client = clientRepository.findByIdOrThrow(request.clientId)
        val products = mutableListOf<Product>()
        var duration = Duration.ZERO
                request.products.forEach {
                    products.add(productRepository.findByIdOrThrow(it)
                        .also { product ->
                            duration += product.duration
                        })
                }

        val daySchedule = scheduleRepository.findByIdOrNull(request.date)
            ?: throw EntityNotFoundException("На этот день (${request.date}) записи (пока) нет")

        validateTimeIsAvailable(request.timeFrom, duration, daySchedule)

        return bookingRepository.save(Booking(
            timeFrom = request.timeFrom,
            duration = duration,
            daySchedule = daySchedule,
            client = client,
            products = products
        )).let { BookingResponse.Mapper.from(it) }

    }

    @Transactional
    @RolesAllowed("admin.bookings:write")
    fun cancelBooking(id: UUID) {
         bookingRepository.findByIdOrThrow(id)
            .apply { timeslotStatus = TimeslotStatus.FREE
                bookingStatus = BookingStatus.CANCELLED}
    }

    @Transactional
    @RolesAllowed("admin.bookings:write")
    fun updateBookingTimeAndProducts(id: UUID, request: BookingTimeAndProductsUpdateRequest) : BookingResponse {
        val booking = bookingRepository.findByIdOrThrow(id)
        booking.timeFrom = request.timeFrom
        if (booking.products.map { it.persistentId } != request.products) {
            booking.products.clear()
            val products = mutableListOf<Product>()
            var duration = Duration.ZERO
            request.products.forEach {
                products.add(productRepository.findByIdOrThrow(it)
                    .also { product ->
                        duration += product.duration
                    })
            }
            booking.duration = duration
            booking.products.addAll(products)
        }

        return bookingRepository.save(booking).let { BookingResponse.Mapper.from(it) }
    }



    private fun validateTimeIsAvailable(timeFrom: LocalTime, duration: Duration, daySchedule: DaySchedule) {
        if (timeFrom < daySchedule.workingTimeFrom || timeFrom.plus(duration) > daySchedule.workingTimeTo) throw UnavailableActionException(
            "Невозможно записаться на нерабочее время"
        )

        if (daySchedule.bookedTimeslots.none { it.timeslotStatus == TimeslotStatus.BUSY }) return

        daySchedule.bookedTimeslots
            .filter { it.timeslotStatus == TimeslotStatus.BUSY }
            .forEach {
            if (timeFrom < it.timeFrom.plus(duration) && timeFrom.plus(duration) > it.timeFrom) throw UnavailableActionException("Недостаточно свободных слотов для записи, выберите другое время")
        }


    }


}