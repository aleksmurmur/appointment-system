package com.aleksmurmur.hairdresser.tests

import com.aleksmurmur.hairdresser.api.SCHEDULE_PATH
import com.aleksmurmur.hairdresser.configuration.Context
import com.aleksmurmur.hairdresser.schedule.domain.DaySchedule
import com.aleksmurmur.hairdresser.booking.domain.TimeslotStatus
import com.aleksmurmur.hairdresser.schedule.dto.DayScheduleCreateOrUpdateRequest
import com.aleksmurmur.hairdresser.schedule.dto.DayScheduleResponse
import com.aleksmurmur.hairdresser.schedule.dto.TimetableCreateRequest
import com.example.*
import com.fasterxml.jackson.core.type.TypeReference
import mu.KLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.util.LinkedMultiValueMap
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ScheduleApiTests : Context() {

    companion object : KLogging()

    lateinit var daySchedule1: DaySchedule
    lateinit var daySchedule2: DaySchedule
    lateinit var daySchedule1PlusDay: DaySchedule
    lateinit var daySchedule1MinusDayWithBookedTimeslot: DaySchedule

    @BeforeEach
    fun before() {
        cleanDB()
        scheduleRepository.save(daySchedule()).run { daySchedule1 = this }
        scheduleRepository.save(daySchedule(workingDay = false)).run { daySchedule2 = this }
        scheduleRepository.save(daySchedule(daySchedule1.persistentId.plusDays(1))).run { daySchedule1PlusDay = this }
        scheduleRepository.save(daySchedule(daySchedule1.persistentId.minusDays(1)))
            .apply {
                bookedTimeslots.add(
                    timeslotRepository.save(timeslot(this))
                )
            }.run { daySchedule1MinusDayWithBookedTimeslot = this }
    }

    @Nested
    inner class GetById {
        @Test
        fun `returns schedule by date`() {
            getSchedule(daySchedule1.persistentId)
                .andExpect { status { isOk() } }
                .response<DayScheduleResponse>(mapper)
                .run {
                    assertEquals(daySchedule1.persistentId, date)
                    assertEquals(daySchedule1.workingDay, workingDay)
                    assertEquals(daySchedule1.workingTimeFrom, workingTimeFrom)
                    assertEquals(daySchedule1.workingTimeTo, workingTimeTo)
                    assertEquals(1, timeslots.size)
                }
        }

        @Test
        fun `returns 404 if non-existent id handled`() {
            getSchedule(LocalDate.now().minusYears(1))
                .andExpect { status { isNotFound() } }
        }

        private fun getSchedule(dateId: LocalDate) =
            testClient.get("$SCHEDULE_PATH/$dateId")

    }

    @Nested
    inner class GetSchedules {
        @Test
        fun `returns one schedule`() {
            getSchedules(daySchedule1.persistentId, daySchedule1.persistentId)
                .andExpect { status { isOk() } }
                .response(mapper, object : TypeReference<List<DayScheduleResponse>>() {})
                .run {
                    assertEquals(1, size)
                    assertEquals(daySchedule1.workingTimeFrom, first().workingTimeFrom)
                }
        }

        @Test
        fun `returns schedules between dates`() {
            val dateFrom = minOf(daySchedule1.persistentId, daySchedule2.persistentId)
            val dateTo = maxOf(daySchedule1.persistentId, daySchedule2.persistentId)
            getSchedules(dateFrom, dateTo)
                .andExpect { status { isOk() } }
                .response(mapper, object : TypeReference<List<DayScheduleResponse>>() {})
                .run {
                    assertEquals(3, size)
                }
        }

        @Test
        fun `returns empty list between non-valid dates`() {
            val dateFrom = minOf(daySchedule1.persistentId, daySchedule2.persistentId)
            val dateTo = maxOf(daySchedule1.persistentId, daySchedule2.persistentId)
            getSchedules(dateTo, dateFrom)
                .andExpect { status { isOk() } }
                .response(mapper, object : TypeReference<List<DayScheduleResponse>>() {})
                .run {
                    assertEquals(0, size)
                }
        }

        private fun getSchedules(dateFrom: LocalDate, dateTo: LocalDate) =
            testClient.get(SCHEDULE_PATH) {
                params = LinkedMultiValueMap<String, String>()
                    .apply {
                        setAll(
                            mapOf(
                                "dateFrom" to dateFrom.format(DateTimeFormatter.ISO_DATE),
                                "dateTo" to dateTo.format(DateTimeFormatter.ISO_DATE)
                            )
                        )
                    }
            }
    }

    @Nested
    inner class Create {
        @Test
        fun `create day schedule`() {
            val request = dayScheduleCreateOrUpdateRequest()
            createDaySchedule(request)
                .andExpect { status { isOk() } }
                .response<DayScheduleResponse>(mapper)
                .run {
                    assertEquals(request.date, date)
                    assertEquals(request.timeFrom, workingTimeFrom)
                    assertEquals(request.timeTo, workingTimeTo)
                    assertEquals(request.workingDay, workingDay)
                }
        }

        @Test
        fun `returns bad request if date is in past`() {
            val request = dayScheduleCreateOrUpdateRequest().copy(date = LocalDate.now().minusDays(1))
            createDaySchedule(request)
                .andExpect { status { isBadRequest() } }
        }

        private fun createDaySchedule(request: DayScheduleCreateOrUpdateRequest) =
            testClient.post(SCHEDULE_PATH) {
                content = mapper.writeValueAsString(request)
                contentType = MediaType.APPLICATION_JSON
            }
    }

    @Nested
    inner class CreateTimetable {
        @Test
        fun `creates timetable`() {
            cleanDB()
            val request = timetableCreateRequest()
            createTimetable(request)
                .andExpect { status { isOk() } }
                .response(mapper, object : TypeReference<List<DayScheduleResponse>>() {})
                .run {
                    assertEquals(request.dateTo.toEpochDay() - request.dateFrom.toEpochDay(), size.toLong())
                    assertEquals(
                        request.workingSchedule[DayOfWeek.MONDAY]!!.timeFrom,
                        first { it.date.dayOfWeek == DayOfWeek.MONDAY }.workingTimeFrom
                    )
                    assertEquals(
                        request.workingSchedule[DayOfWeek.MONDAY]!!.timeTo,
                        first { it.date.dayOfWeek == DayOfWeek.MONDAY }.workingTimeTo
                    )
                    assertEquals(
                        request.workingSchedule[DayOfWeek.MONDAY]!!.workingDay,
                        first { it.date.dayOfWeek == DayOfWeek.MONDAY }.workingDay
                    )
                }
        }

        @Test
        fun `returns 400 if already has schedules between dates`() {
            val request = timetableCreateRequest().copy(dateTo = daySchedule1.persistentId)
            createTimetable(request)
                .andExpect { status { isBadRequest() } }
        }

        private fun createTimetable(request: TimetableCreateRequest) =
            testClient.post("$SCHEDULE_PATH/timetable") {
                content = mapper.writeValueAsString(request)
                contentType = MediaType.APPLICATION_JSON
            }
    }

    @Nested
    inner class Update {
        @Test
        fun `updates day schedule`() {
            val request = dayScheduleCreateOrUpdateRequest().copy(date = daySchedule1.persistentId)
            updateDaySchedule(request)
                .andExpect { status { isOk() } }
                .response<DayScheduleResponse>(mapper)
                .run {
                    assertEquals(request.date, date)
                    assertEquals(request.timeFrom, workingTimeFrom)
                    assertEquals(request.timeTo, workingTimeTo)
                    assertEquals(request.workingDay, workingDay)
                }
        }

        @Test
        fun `returns 404 if non-existent id handled`() {
            val request = dayScheduleCreateOrUpdateRequest().copy(date = LocalDate.now())
            updateDaySchedule(request)
                .andExpect { status { isNotFound() } }
        }

        @Test
        fun `returns bad request making holiday if has scheduled visits`() {
            val request = dayScheduleCreateOrUpdateRequest().copy(
                date = daySchedule1MinusDayWithBookedTimeslot.persistentId,
                workingDay = false
            )
            updateDaySchedule(request)
                .andExpect { status { isBadRequest() } }
        }


        @Test
        fun `returns bad request if has start time conflict`() {
            val request = dayScheduleCreateOrUpdateRequest().copy(
                date = daySchedule1MinusDayWithBookedTimeslot.persistentId,
                timeFrom = daySchedule1MinusDayWithBookedTimeslot.bookedTimeslots.first().timeFrom.plusSeconds(1),
                workingDay = true
            )
            updateDaySchedule(request)
                .andExpect { status { isBadRequest() } }
        }

        @Test
        fun `returns bad request if has end time conflict`() {
            val request = dayScheduleCreateOrUpdateRequest().copy(
                date = daySchedule1MinusDayWithBookedTimeslot.persistentId,
                timeTo = daySchedule1MinusDayWithBookedTimeslot.bookedTimeslots.last()
                    .let { it.timeFrom.plus(it.duration).minusSeconds(1)},
                workingDay = true
            )
            updateDaySchedule(request)
                .andExpect { status { isBadRequest() } }
        }

        @Test
        fun `updates if has no time conflict`() {
            val request = dayScheduleCreateOrUpdateRequest().copy(
                date = daySchedule1MinusDayWithBookedTimeslot.persistentId,
                timeFrom = daySchedule1MinusDayWithBookedTimeslot.bookedTimeslots.first().timeFrom.minusSeconds(1),
                timeTo = daySchedule1MinusDayWithBookedTimeslot.bookedTimeslots.last()
                    .let { it.timeFrom.plus(it.duration).plusSeconds(1)},
                workingDay = true
            )
            updateDaySchedule(request)
                .andExpect { status { isOk() } }
                .response<DayScheduleResponse>(mapper)
                .run {
                    assertEquals(request.timeFrom, workingTimeFrom)
                    assertEquals(request.timeTo, workingTimeTo)
                    assertEquals(1, timeslots.filter { it.status == TimeslotStatus.BUSY }.size)
                }
        }

        private fun updateDaySchedule(request: DayScheduleCreateOrUpdateRequest) =
            testClient.patch(SCHEDULE_PATH) {
                content = mapper.writeValueAsString(request)
                contentType = MediaType.APPLICATION_JSON
            }
    }

    @Nested
    inner class Delete {
        @Test
        fun `deletes day schedule`() {
            deleteSchedule(daySchedule1.persistentId)
                .andExpect { status { isNoContent() } }
        }

        @Test
        fun `returns 404 if non-existent id handled`() {
            deleteSchedule(LocalDate.now().minusDays(1))
                .andExpect { status { isNotFound() } }
        }

        @Test
        fun `returns bad request if has scheduled timeslots`() {
            deleteSchedule(daySchedule1MinusDayWithBookedTimeslot.persistentId)
                .andExpect { status { isBadRequest() } }
        }

        private fun deleteSchedule(id: LocalDate) =
            testClient.delete("$SCHEDULE_PATH/$id")
    }

    @Nested
    inner class DeleteBetweenDates {
        @Test
        fun `deletes schedules`() {
            deletesSchedules(daySchedule1.persistentId, daySchedule1PlusDay.persistentId)
                .andExpect { status { isNoContent() } }
        }

        @Test
        fun `returns 400 if has booked timeslots`() {
            deletesSchedules(daySchedule1MinusDayWithBookedTimeslot.persistentId, daySchedule1PlusDay.persistentId)
                .andExpect { status { isBadRequest() } }
        }

        private fun deletesSchedules(dateFrom: LocalDate, dateTo: LocalDate) =
            testClient.delete(SCHEDULE_PATH) {
                params = LinkedMultiValueMap<String, String>()
                    .apply {
                        setAll(
                            mapOf(
                                "dateFrom" to dateFrom.format(DateTimeFormatter.ISO_DATE),
                                "dateTo" to dateTo.format(DateTimeFormatter.ISO_DATE)
                            )
                        )
                    }
            }
    }
}

