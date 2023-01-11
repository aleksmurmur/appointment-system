package com.example

import com.aleksmurmur.hairdresser.client.domain.Client
import com.aleksmurmur.hairdresser.client.dto.ClientCreateRequest
import com.aleksmurmur.hairdresser.client.dto.ClientUpdateRequest
import com.aleksmurmur.hairdresser.product.domain.Product
import com.aleksmurmur.hairdresser.product.dto.ProductCreateRequest
import com.aleksmurmur.hairdresser.product.dto.ProductUpdateRequest
import com.aleksmurmur.hairdresser.schedule.domain.DaySchedule
import com.aleksmurmur.hairdresser.booking.domain.Timeslot
import com.aleksmurmur.hairdresser.booking.domain.TimeslotStatus
import com.aleksmurmur.hairdresser.booking.dto.TimeslotCreateOrUpdateRequest
import com.aleksmurmur.hairdresser.schedule.dto.DayScheduleCreateOrUpdateRequest
import com.aleksmurmur.hairdresser.schedule.dto.TimetableCreateRequest
import com.aleksmurmur.hairdresser.schedule.dto.TimetableWorkingSlot
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.random.Random

fun client(phone: String? = null) = Client(
    phone = phone ?: randomPhone(),
    name = randomString()
)

fun clientCreateRequest(noName: Boolean = false) = ClientCreateRequest(
    phone = randomPhone(),
    name = if (noName) null else randomString()
)

fun clientUpdateRequest() = ClientUpdateRequest(
    phone = randomPhone(),
    name = randomString()
)

fun daySchedule(date: LocalDate? = null, workingDay: Boolean = true) = DaySchedule(
    date = date ?: randomFutureDate(),
    workingDay = workingDay,
    workingTimeFrom = if (workingDay) randomLocalTime() else null,
    workingTimeTo = null
).apply { if (workingDay) workingTimeTo = randomLocalTime(workingTimeFrom!!.toSecondOfDay().toLong()) }

fun dayScheduleCreateOrUpdateRequest() = DayScheduleCreateOrUpdateRequest(
    date = randomFutureDate(),
    workingDay = Random.nextBoolean(),
    timeFrom = randomLocalTime(),
    timeTo = randomLocalTime()
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

fun product(deleted: Boolean = false) = Product(
    randomString(),
    randomString(),
    Random.nextLong(),
    randomDuration(),
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
