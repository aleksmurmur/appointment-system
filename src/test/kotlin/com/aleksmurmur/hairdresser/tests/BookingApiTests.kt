package com.aleksmurmur.hairdresser.tests

import com.aleksmurmur.hairdresser.api.BOOKING_PATH
import com.aleksmurmur.hairdresser.api.TIMESLOTS_PATH
import com.aleksmurmur.hairdresser.booking.domain.Booking
import com.aleksmurmur.hairdresser.booking.domain.BookingStatus
import com.aleksmurmur.hairdresser.booking.domain.TimeslotStatus
import com.aleksmurmur.hairdresser.booking.dto.BookingCreateRequest
import com.aleksmurmur.hairdresser.booking.dto.BookingResponse
import com.aleksmurmur.hairdresser.booking.dto.BookingTimeAndProductsUpdateRequest
import com.aleksmurmur.hairdresser.booking.dto.TimeslotResponse
import com.aleksmurmur.hairdresser.client.domain.Client
import com.aleksmurmur.hairdresser.configuration.Context
import com.aleksmurmur.hairdresser.product.domain.Product
import com.aleksmurmur.hairdresser.schedule.domain.DaySchedule
import com.example.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import com.fasterxml.jackson.core.type.TypeReference
import mu.KLogging
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class BookingApiTests : Context() {

    lateinit var booking1: Booking
    lateinit var booking2: Booking
    lateinit var client: Client
    lateinit var daySchedule1: DaySchedule
    lateinit var daySchedule2: DaySchedule
    lateinit var dayScheduleFree: DaySchedule
    lateinit var product: Product
    lateinit var shorterProduct: Product
    lateinit var productForFreeSchedule: Product
    lateinit var timelessProduct: Product
    lateinit var daySchedule9To20: DaySchedule
    lateinit var booking12To15: Booking
    lateinit var product180min: Product

    companion object : KLogging()


    @BeforeEach

    fun before() {
        cleanDB()
        clientRepository.save(client()).run { client = this }
        scheduleRepository.save(daySchedule()).run { daySchedule1 = this }
        bookingRepository.save(booking(daySchedule1, client)
            .apply {
                products.add(
                    product(false, this.duration)
                        .apply {
                            productRepository.save(this)
                         product = this })
            }).run { booking1 = this }
        scheduleRepository.save(daySchedule(daySchedule1.persistentId.plusDays(1))).run { daySchedule2 = this }
        bookingRepository.save(booking(daySchedule2, client)).run { booking2 = this }
        scheduleRepository.save(daySchedule()).run { dayScheduleFree = this }
        productRepository.save(product(false, randomDuration(0, Duration.between(dayScheduleFree.workingTimeFrom!!, dayScheduleFree.workingTimeTo!!).toMinutes())))
            .run { productForFreeSchedule = this }
        productRepository.save(product(false,
            randomDuration(0, product.duration.toMinutes()))).run { shorterProduct = this }
        productRepository.save(product(false,
        Duration.ofHours(25)
        )).run { timelessProduct = this }
        scheduleRepository.save(DaySchedule(
            LocalDate.now().plusDays(1),
        LocalTime.of(9,0),
        LocalTime.of(20, 0),
        true)).run { daySchedule9To20 = this }
        productRepository.save(Product(
            name = "qwerty",
            price = 1000,
            duration = Duration.ofMinutes(180)
        )).run { product180min = this }
        bookingRepository.save(Booking(
            LocalTime.of(12, 0),
            duration = product180min.duration,
            daySchedule = daySchedule9To20,
            client = client,
            products = mutableListOf(product180min)
        )).run { booking12To15 = this }

    }

    @Nested
    inner class GetById {
        @Test
        fun `returns booking by id`() {
            getBooking(booking1.persistentId)
                .andExpect { status { isOk() } }
                .response<BookingResponse>(mapper)
                .run {
                    assertEquals(booking1.persistentId, id)
                    assertEquals(booking1.timeFrom, timeFrom)
                    assertEquals(booking1.duration, duration)
                    assertEquals(booking1.daySchedule.persistentId, date)
                    assertEquals(booking1.bookingStatus, status)
                    assertEquals(booking1.client.persistentId, client.id)
                    assertEquals(booking1.products.first().persistentId, products.first().id)
                }
        }

        @Test
        fun `returns 404 if non-existent id handled`() {
            getBooking(UUID.randomUUID())
                .andExpect { status { isNotFound() } }
        }


        private fun getBooking(id: UUID) =
            testClient.get("$BOOKING_PATH/$id")
    }

    @Nested
    inner class GetAllByClient {
        @Test
        fun `returns client bookings`() {
            getClientBookings(client.persistentId)
                .andExpect { status { isOk() } }
                .response(mapper, object : TypeReference<List<BookingResponse>>() {})
                .run {
                    assertEquals(3, size)
                    assertEquals(booking1.persistentId, first().id)
                    assertEquals(booking2.persistentId, get(1).id)
                    assertEquals(booking12To15.persistentId, last().id)
                }
        }

        @Test
        fun `returns 404 if non-existent client id handled`() {
            getClientBookings(UUID.randomUUID())
                .andExpect { status { isNotFound() } }
        }

        private fun getClientBookings(clientId: UUID) =
            testClient.get("$BOOKING_PATH/clients/$clientId")
    }

    @Nested
    inner class CreateBooking {
        @Test
        fun `creates booking`() {
            val request = bookingCreateRequest(dayScheduleFree, client.persistentId, listOf(productForFreeSchedule))
            createBooking(request)
                .andExpect { status { isOk() } }
                .response<BookingResponse>(mapper)
                .run {
                    assertEquals(request.date, date)
                    assertEquals(request.timeFrom, timeFrom)
                    assertEquals(request.clientId, client.id)
                    assertEquals(request.products.first(), products.first().id)
                }
        }

        @Test
        fun `returns 400 if past date handled`() {
            createBooking(randomBookingCreateRequest().copy(date = LocalDate.now().minusDays(1)))
                .andExpect { status { isBadRequest() } }
        }

        @Test
        fun `returns 404 if no schedule exists for date`() {
            createBooking(bookingCreateRequest(dayScheduleFree, client.persistentId, listOf(productForFreeSchedule)).copy(date = randomFutureDate()))
                .andExpect { status { isNotFound() } }
        }

        @Test
        fun `returns 404 if non-existent client id handled`() {
            createBooking(bookingCreateRequest(dayScheduleFree, UUID.randomUUID(), listOf(productForFreeSchedule)))
                .andExpect { status { isNotFound() } }
        }

        @Test
        fun `returns 400 if products list is empty`() {
            createBooking(bookingCreateRequest(dayScheduleFree, client.persistentId))
                .andExpect { status { isBadRequest() } }
        }


        @Test
        fun `returns 400 if time is not available - other booking 2`() {
            createBooking(
                BookingCreateRequest(
                    daySchedule9To20.persistentId,
                    LocalTime.of(10, 0),
                    client.persistentId,
                    listOf(product180min.persistentId)
                ))
                     .andExpect { status { isBadRequest() } }
        }

        @Test
        fun `returns 400 if time is not available - other booking 3`() {
            createBooking(
                BookingCreateRequest(
                    daySchedule9To20.persistentId,
                    LocalTime.of(14, 0),
                    client.persistentId,
                    listOf(product180min.persistentId)
                ))
                .andExpect { status { isBadRequest() } }
        }

        @Test
        fun `returns 400 if time is not available - other booking 4`() {
            createBooking(
                BookingCreateRequest(
                    daySchedule9To20.persistentId,
                    LocalTime.of(12, 0),
                    client.persistentId,
                    listOf(product180min.persistentId)
                ))
                .andExpect { status { isBadRequest() } }
        }

        @Test
        fun `successfully books other booking a day`() {
            createBooking(
                BookingCreateRequest(
                    daySchedule9To20.persistentId,
                    LocalTime.of(15, 0),
                    client.persistentId,
                    listOf(product180min.persistentId)
                ))
                .andExpect { status { isOk() } }
        }
        @Test
        fun `successfully books other booking a day 2`() {
            createBooking(
                BookingCreateRequest(
                    daySchedule9To20.persistentId,
                    LocalTime.of(9, 0),
                    client.persistentId,
                    listOf(product180min.persistentId)
                ))
                .andExpect { status { isOk() } }
        }

        @Test
        fun `returns 400 if time is not available - not working hours`() {
            createBooking(
                BookingCreateRequest(
                    daySchedule9To20.persistentId,
                    LocalTime.of(8, 0),
                    client.persistentId,
                    listOf(product180min.persistentId)
                ))
                .andExpect { status { isBadRequest() } }
        }

        @Test
        fun `returns 400 if time is not available - not working hours 2`() {
            createBooking(
                BookingCreateRequest(
                    daySchedule9To20.persistentId,
                    LocalTime.of(19, 0),
                    client.persistentId,
                    listOf(product180min.persistentId)
                ))
                .andExpect { status { isBadRequest() } }
        }

        @Test
        fun `returns 400 if time is not available - not working hours 3`() {
            createBooking(BookingCreateRequest(booking1.daySchedule.persistentId, booking1.daySchedule.workingTimeFrom!!.minusMinutes(1), booking1.client.persistentId, booking1.products.map { it.persistentId }))
                .andExpect { status { isBadRequest() } }
        }

        private fun createBooking(request: BookingCreateRequest) =
            testClient.post(BOOKING_PATH) {
                content = mapper.writeValueAsString(request)
                contentType = MediaType.APPLICATION_JSON
            }
    }

    @Nested
    inner class Cancel {
        @Test
        fun `cancels booking making timeslot free`() {
            cancelBooking(booking1.persistentId)
                .andExpect { status { isNoContent() } }
            getBookingById(booking1.persistentId)
                .andExpect { status { isOk() } }
                .response<BookingResponse>(mapper)
                .run {
                    assertEquals(BookingStatus.CANCELLED, status)
                }
            getTimeslotById(booking1.persistentId)
                .andExpect { status { isOk() } }
                .response<TimeslotResponse>(mapper)
                .run {
                    assertEquals(TimeslotStatus.FREE, status)
                }
        }

        @Test
        fun `returns 404 if non-existent client id handled`() {
            cancelBooking(UUID.randomUUID())
                .andExpect { status { isNotFound() } }
        }


        private fun cancelBooking(id: UUID) =
            testClient.delete("$BOOKING_PATH/$id")
        private fun getBookingById(id: UUID) =
            testClient.get("$BOOKING_PATH/$id")
        private fun getTimeslotById(id: UUID) =
            testClient.get("$TIMESLOTS_PATH/$id")

    }

    @Nested
    inner class Update {
        @Test
        fun `updates booking time`() {
            val request = BookingTimeAndProductsUpdateRequest(daySchedule1.workingTimeFrom!!, booking1.products.map { it.persistentId })
            updateBooking(booking1.persistentId, request)
                .andExpect { status { isOk() } }
                .response<BookingResponse>(mapper)
                .run {
                    assertEquals(booking1.persistentId, id)
                    assertEquals(request.timeFrom, timeFrom)
                    assertEquals(booking1.duration, duration)
                    assertEquals(booking1.daySchedule.persistentId, date)
                    assertEquals(booking1.bookingStatus, status)
                    assertEquals(booking1.client.persistentId, client.id)
                    assertEquals(booking1.products.size, products.size)
                    assertEquals(request.products.size, products.size)
                    assertEquals(booking1.products.first().persistentId, products.first().id)
                }
        }

        @Test
        fun `updates time and products`() {
            val request = BookingTimeAndProductsUpdateRequest(
                randomLocalTime(daySchedule1.workingTimeFrom!!, booking1.timeFrom),
                listOf(shorterProduct.persistentId)
            )
            updateBooking(booking1.persistentId, request)
                .andExpect { status { isOk() } }
                .response<BookingResponse>(mapper)
                .run {
                    assertEquals(booking1.persistentId, id)
                    assertEquals(request.timeFrom, timeFrom)
                    assertEquals(shorterProduct.duration, duration)
                    assertEquals(booking1.daySchedule.persistentId, date)
                    assertEquals(booking1.bookingStatus, status)
                    assertEquals(booking1.client.persistentId, client.id)
                    assertEquals(booking1.products.size, products.size)
                    assertEquals(request.products.first(), products.first().id)
                }
        }

        @Test
        fun `returns 404 if non-existent client id handled`() {
            updateBooking(UUID.randomUUID(), BookingTimeAndProductsUpdateRequest(LocalTime.now(), listOf(product.persistentId)))
                .andExpect { status { isNotFound() } }
        }

        @Test
        fun `returns 400 if products list is empty`() {
            updateBooking(booking1.persistentId, BookingTimeAndProductsUpdateRequest(booking1.timeFrom, listOf()))
                .andExpect { status { isBadRequest() } }
        }

//        @Test
//        fun `returns 400 if time is not available - other booking`() {
//            updateBooking(booking1.persistentId,
//                BookingTimeAndProductsUpdateRequest())
//                .andExpect { status { isBadRequest() } }
//        }

        @Test
        fun `returns 400 if time is not available - not working hours`() {
            updateBooking(booking1.persistentId,
                BookingTimeAndProductsUpdateRequest(booking1.daySchedule.workingTimeFrom!!.minusSeconds(1), booking1.products.map { it.persistentId }))
                .andExpect { status { isBadRequest() } }
        }

        @Test
        fun `returns 400 if time is not available - not working hours 2`() {
            updateBooking(booking1.persistentId,
                BookingTimeAndProductsUpdateRequest(booking1.timeFrom, listOf(timelessProduct.persistentId, product.persistentId)))
                .andExpect { status { isBadRequest() } }
        }


        private fun updateBooking(id: UUID, request: BookingTimeAndProductsUpdateRequest) =
            testClient.patch("$BOOKING_PATH/$id") {
                content = mapper.writeValueAsString(request)
                contentType = MediaType.APPLICATION_JSON
            }
    }

}