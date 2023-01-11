package com.aleksmurmur.hairdresser.tests

import com.aleksmurmur.hairdresser.api.TIMESLOTS_PATH
import com.aleksmurmur.hairdresser.booking.domain.Timeslot
import com.aleksmurmur.hairdresser.booking.domain.TimeslotStatus
import com.aleksmurmur.hairdresser.booking.dto.TimeslotCreateOrUpdateRequest
import com.aleksmurmur.hairdresser.booking.dto.TimeslotResponse
import com.aleksmurmur.hairdresser.configuration.Context
import com.aleksmurmur.hairdresser.schedule.domain.DaySchedule
import com.example.*
import com.fasterxml.jackson.core.type.TypeReference
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.util.LinkedMultiValueMap
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class TimeslotApiTests :Context() {


    lateinit var unavailableTimeslot1 : Timeslot
    lateinit var unavailableTimeslot2 : Timeslot
    lateinit var busyTimeslot: Timeslot
    lateinit var daySchedule: DaySchedule
    lateinit var dayScheduleBusy: DaySchedule

    @BeforeEach
    fun before() {
        cleanDB()
        scheduleRepository.save(daySchedule()).run { daySchedule = this }
        scheduleRepository.save(daySchedule()).run { dayScheduleBusy = this }
        timeslotRepository.save(timeslot(daySchedule).apply { this.timeslotStatus = TimeslotStatus.UNAVAILABLE }).run { unavailableTimeslot1 = this }
        timeslotRepository.save(timeslot(daySchedule).apply { this.timeslotStatus = TimeslotStatus.UNAVAILABLE }).run { unavailableTimeslot2 = this }
        timeslotRepository.save(timeslot(dayScheduleBusy)).run { busyTimeslot = this }
    }

    @Nested
    inner class GetById {
        @Test
        fun `returns timeslot by id`() {
            getTimeslot(unavailableTimeslot1.persistentId)
                .andExpect { status { isOk() } }
                .response<TimeslotResponse>(mapper)
                .run {
                    assertEquals(unavailableTimeslot1.timeFrom, timeFrom)
                    assertEquals(unavailableTimeslot1.timeFrom.plus(unavailableTimeslot1.duration), timeTo)
                    assertEquals(unavailableTimeslot1.timeslotStatus, status)
                }
        }

        @Test
        fun `returns 404 if non-existent id handled`() {
            getTimeslot(UUID.randomUUID())
                .andExpect { status { isNotFound() } }
        }

        private fun getTimeslot(id: UUID) =
            testClient.get("$TIMESLOTS_PATH/$id")
    }

    @Nested
    inner class GetByDate {
        @Test
        fun `returns unavailable timeslots by date`() {
            getByDate(unavailableTimeslot1.daySchedule.persistentId)
                .andExpect { status { isOk() } }
                .response(mapper, object : TypeReference<List<TimeslotResponse>>() {})
                .run {
                    assertEquals(2, size)
                    assert(any { it.timeFrom == unavailableTimeslot1.timeFrom })
                    assert(any { it.timeFrom == unavailableTimeslot2.timeFrom})
                }
        }

        private fun getByDate(date: LocalDate) =
            testClient.get(TIMESLOTS_PATH) {
                params =  LinkedMultiValueMap<String, String>()
                    .apply {
                        setAll(
                            mapOf(
                                "date" to date.format(DateTimeFormatter.ISO_DATE),
                            )
                        )
                    }
            }

    }

    @Nested
    inner class Create {
        @Test
        fun `creates unavailable timeslot`() {
            val request = timeslotCreateRequest(daySchedule.persistentId).copy(timeFrom = daySchedule.workingTimeFrom!!,
            duration = Duration.between(daySchedule.workingTimeTo, daySchedule.workingTimeFrom))
            createTimeslot(request)
                .andExpect { status { isOk() } }
                .response<TimeslotResponse>(mapper)
                .run {
                    assertEquals(request.timeFrom, timeFrom)
                    assertEquals(request.timeFrom.plus(request.duration), timeTo)
                    assertEquals(request.status, status)
                }
        }

        @Test
        fun `returns 400 in start time conflict`() {
            val request = timeslotCreateRequest(dayScheduleBusy.persistentId)
                .copy(timeFrom = busyTimeslot.timeFrom.minusSeconds(2))
            createTimeslot(request)
                .andExpect { status { isBadRequest() } }
        }
        @Test
        fun `returns 400 in end time conflict`() {
            val request = timeslotCreateRequest(dayScheduleBusy.persistentId)
                .copy(timeFrom = busyTimeslot.timeFrom.plus(busyTimeslot.duration).minusSeconds(2))
            createTimeslot(request)
                .andExpect { status { isBadRequest() } }
        }

        @Test
        fun `returns 404 if non-existent schedule handled`() {
            val request = timeslotCreateRequest(dayScheduleBusy.persistentId)
                .copy(date = randomFutureDate())
            createTimeslot(request)
                .andExpect { status { isNotFound() } }
        }

        private fun createTimeslot(request: TimeslotCreateOrUpdateRequest) =
            testClient.post(TIMESLOTS_PATH) {
                content = mapper.writeValueAsString(request)
                contentType = MediaType.APPLICATION_JSON
            }
    }

    @Nested
    inner class Delete {
    fun `deletes timeslot`() {
        deleteTimeslot(unavailableTimeslot1.persistentId )
            .andExpect { status { isNoContent() } }
    }

        fun `returns 404 if non-existent id handled`() {
            deleteTimeslot(UUID.randomUUID())
                .andExpect { status { isNotFound() } }
        }

        fun `returns 400 if busy timeslot`() {
            deleteTimeslot(busyTimeslot.persistentId)
                .andExpect { status { isBadRequest() } }
        }

    private fun deleteTimeslot(id: UUID) =
        testClient.delete("${TIMESLOTS_PATH}/${id}")

    }
}