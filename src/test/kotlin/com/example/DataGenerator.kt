package com.example

import com.aleksmurmur.hairdresser.booking.domain.Booking
import com.aleksmurmur.hairdresser.client.domain.Client
import com.aleksmurmur.hairdresser.client.dto.ClientCreateOrUpdateRequest
import com.aleksmurmur.hairdresser.product.domain.Product
import com.aleksmurmur.hairdresser.product.dto.ProductCreateRequest
import com.aleksmurmur.hairdresser.product.dto.ProductUpdateRequest
import com.aleksmurmur.hairdresser.schedule.domain.DaySchedule
import com.aleksmurmur.hairdresser.booking.domain.Timeslot
import com.aleksmurmur.hairdresser.booking.domain.TimeslotStatus
import com.aleksmurmur.hairdresser.booking.dto.BookingCreateRequest
import com.aleksmurmur.hairdresser.booking.dto.TimeslotCreateOrUpdateRequest
import com.aleksmurmur.hairdresser.schedule.dto.DayScheduleCreateOrUpdateRequest
import com.aleksmurmur.hairdresser.schedule.dto.TimetableCreateRequest
import com.aleksmurmur.hairdresser.schedule.dto.TimetableWorkingSlot
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.util.UUID
import kotlin.random.Random

fun client(phone: String? = null) = Client(
    phone = phone ?: randomPhone(),
    name = randomString()
)

fun clientCreateOrUpdateRequest(noName: Boolean = false) = ClientCreateOrUpdateRequest(
    phone = randomPhone(),
    name = if (noName) null else randomString()
)



fun daySchedule(date: LocalDate? = null, workingDay: Boolean = true) = DaySchedule(
    date = date ?: randomFutureDate(),
    workingDay = workingDay,
    workingTimeFrom = if (workingDay) randomLocalTime() else null,
    workingTimeTo = null
).apply { if (workingDay) workingTimeTo = randomLocalTime(workingTimeFrom!!) }

fun dayScheduleCreateOrUpdateRequest() = DayScheduleCreateOrUpdateRequest(
    date = randomFutureDate(),
    workingDay = Random.nextBoolean(),
    timeFrom = randomLocalTime(),
    timeTo = randomLocalTime()
)

fun booking(daySchedule: DaySchedule, client: Client) = Booking(
    timeFrom = randomLocalTime(daySchedule.workingTimeFrom!!, daySchedule.workingTimeTo!!),
    duration = randomDuration(0, Duration.between(daySchedule.workingTimeFrom!!, daySchedule.workingTimeTo!!).toMinutes()),
    daySchedule = daySchedule,
    timeslotStatus = TimeslotStatus.BUSY,
    client = client,
    products = mutableListOf()
)

fun bookingCreateRequest(daySchedule: DaySchedule, clientId: UUID, products: List<Product> = listOf()) : BookingCreateRequest {
    val timeFrom = randomLocalTime(daySchedule.workingTimeFrom!!, daySchedule.workingTimeTo!!.minusMinutes(products.sumOf { it.duration.toMinutes() }))
    return BookingCreateRequest(
            date = daySchedule.persistentId,
    timeFrom = timeFrom,
    clientId = clientId,
    products = products.map { it.persistentId }
) }

fun randomBookingCreateRequest() = BookingCreateRequest(
    randomFutureDate(),
    randomLocalTime(),
    UUID.randomUUID(),
    listOf()
)

fun timeslot(schedule: DaySchedule) = Timeslot(
    randomLocalTime(),
    randomDuration(),
    schedule,
    TimeslotStatus.BUSY
)

fun timeslotCreateRequest(date: LocalDate? = null, status: TimeslotStatus = TimeslotStatus.UNAVAILABLE) = TimeslotCreateOrUpdateRequest(
    date ?: randomFutureDate(),
    randomLocalTime(),
    randomDuration(),
    status
)

fun timetableCreateRequest() = TimetableCreateRequest(
    LocalDate.now(),
    LocalDate.now().plusDays(Random.nextLong(365)),
    buildMap { DayOfWeek.values().forEach { this[it] = timetableWorkingSlot() } }
)

fun timetableWorkingSlot() = TimetableWorkingSlot(
    randomLocalTime(),
    randomLocalTime(),
    Random.nextBoolean()
)

fun product(deleted: Boolean = false, duration: Duration? = null) = Product(
    randomString(),
    randomString(),
    Random.nextLong(),
    duration ?: randomDuration(),
    deleted
)

fun productCreateRequest() = ProductCreateRequest(
    randomString(),
    randomString(),
    Random.nextLong(0, Long.MAX_VALUE),
    randomDuration().toMinutes()
)

fun productUpdateRequest() = ProductUpdateRequest(
    randomString(),
    randomString(),
    Random.nextLong(0, Long.MAX_VALUE),
    randomDuration().toMinutes()
)
