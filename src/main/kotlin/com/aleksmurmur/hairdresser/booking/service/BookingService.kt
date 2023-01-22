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
import com.aleksmurmur.hairdresser.exception.BadRequestException
import jakarta.annotation.security.RolesAllowed
import mu.KLogging
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
    val productRepository: ProductRepository,
    val scheduleRepository: ScheduleRepository
) {

    companion object: KLogging()


    @Transactional(readOnly = true)
    @RolesAllowed("admin.bookings:read")
    fun getById(id: UUID): BookingResponse =
        bookingRepository.findByIdOrThrow(id)
            .let {
                BookingResponse.Mapper.from(it) }

    @Transactional(readOnly = true)
    @RolesAllowed("admin.bookings:read")
    fun getAllActiveByClient(clientId: UUID): List<BookingResponse> =
        bookingRepository.findByClientAndBookingStatusEquals(clientRepository.findByIdOrThrow(clientId), BookingStatus.BOOKED)
            .sortedWith(compareBy<Booking> { it.daySchedule.persistentId}.thenBy{ it.timeFrom } )
            .map { BookingResponse.Mapper.from(it) }

    @Transactional(readOnly = true)
    @RolesAllowed("admin.bookings:read")
    fun getAllByClient(clientId: UUID): List<BookingResponse> =
        bookingRepository.findByClient(clientRepository.findByIdOrThrow(clientId))
            .sortedWith(compareBy<Booking> { it.daySchedule.persistentId}.thenBy{ it.timeFrom } )
            .map { BookingResponse.Mapper.from(it) }

    @Transactional(readOnly = true)
    @RolesAllowed("admin.bookings:read")
    fun getAllActive(): List<BookingResponse> =
        bookingRepository.findAllByBookingStatusEquals()
            .sortedWith(compareBy<Booking> { it.daySchedule.persistentId}.thenBy{ it.timeFrom } )
            .map { BookingResponse.Mapper.from(it) }

    @Transactional
    @RolesAllowed("admin.bookings:write")
    fun createBooking(request: BookingCreateRequest): BookingResponse {
        if (request.products.isEmpty()) throw BadRequestException("Не выбрано ни ождной услуги / товара")
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

        return bookingRepository.save(
            Booking(
                timeFrom = request.timeFrom,
                duration = duration,
                daySchedule = daySchedule,
                client = client,
                products = products
            )
        ).let { BookingResponse.Mapper.from(it) }
            .also { logger.debug { """
               Created booking with id: ${it.id} 
            """ } }

    }

    @Transactional
    @RolesAllowed("admin.bookings:write")
    fun cancelBooking(id: UUID) {
        bookingRepository.findByIdOrThrow(id)
            .apply {
                timeslotStatus = TimeslotStatus.FREE
                bookingStatus = BookingStatus.CANCELLED
            }
            .also { logger.debug { """
               Canceled booking with id: ${it.id} 
            """ } }
    }

    @Transactional
    @RolesAllowed("admin.bookings:write")
    fun updateBookingTimeAndProducts(id: UUID, request: BookingTimeAndProductsUpdateRequest): BookingResponse {
        if (request.products.isEmpty()) throw BadRequestException("Не выбрано ни одной услуги / товара")

        val booking = bookingRepository.findByIdOrThrow(id)

        val products = getProducts(booking, request)

        val duration = Duration.ofMinutes(products.sumOf { it.duration.toMinutes() })

        validateTimeIsAvailable(request.timeFrom, duration, booking.daySchedule, booking.persistentId)

        booking.timeFrom = request.timeFrom
        if (products != booking.products) {
            booking.duration = duration
            booking.products.clear()
            booking.products.addAll(products)
        }
        return bookingRepository.save(booking).let { BookingResponse.Mapper.from(it) }
            .also { logger.debug { """
               Updated booking with id: ${it.id} 
            """ } }
    }

    private fun getProducts(booking: Booking, request: BookingTimeAndProductsUpdateRequest): MutableList<Product> {
        val products = mutableListOf<Product>()
        if (booking.products.map { it.persistentId } != request.products) {
            request.products.forEach {
                products.add(productRepository.findByIdOrThrow(it))
            }
        } else products.addAll(booking.products)
        return products
    }


    private fun validateTimeIsAvailable(timeFrom: LocalTime, duration: Duration, daySchedule: DaySchedule, currentBooking: UUID? = null) {
        if (Duration.between(timeFrom, daySchedule.workingTimeTo) < duration) throw UnavailableActionException("Выбранные услуги не укладываются в рабочее время")
        if (timeFrom < daySchedule.workingTimeFrom || timeFrom.plus(duration) > daySchedule.workingTimeTo) throw UnavailableActionException(
            "Невозможно записаться на нерабочее время"
        )

        if (daySchedule.bookedTimeslots.none { it.timeslotStatus != TimeslotStatus.FREE }) return

        daySchedule.bookedTimeslots
            .filter { it.timeslotStatus != TimeslotStatus.FREE }
            .filter { it.persistentId != currentBooking }
            .forEach {
                if (timeFrom < it.timeFrom.plus(it.duration) && timeFrom.plus(duration) > it.timeFrom)
                     throw UnavailableActionException(
                    "Недостаточно свободных слотов для записи, выберите другое время"
                )
            }


    }


}