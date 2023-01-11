package com.aleksmurmur.hairdresser.booking.service

import com.aleksmurmur.hairdresser.booking.domain.Timeslot
import com.aleksmurmur.hairdresser.booking.domain.TimeslotStatus
import com.aleksmurmur.hairdresser.booking.dto.TimeslotCreateOrUpdateRequest
import com.aleksmurmur.hairdresser.booking.dto.TimeslotResponse
import com.aleksmurmur.hairdresser.booking.repository.TimeslotRepository
import com.aleksmurmur.hairdresser.common.jpa.findByIdOrThrow
import com.aleksmurmur.hairdresser.exception.BadRequestException
import com.aleksmurmur.hairdresser.exception.EntityNotFoundException
import com.aleksmurmur.hairdresser.exception.UnavailableActionException
import com.aleksmurmur.hairdresser.schedule.domain.DaySchedule
import com.aleksmurmur.hairdresser.schedule.repository.ScheduleRepository
import jakarta.annotation.security.RolesAllowed
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@Service
class TimeslotService(
    val timeslotRepository: TimeslotRepository,
    val scheduleRepository: ScheduleRepository
) {

    @Transactional
    @RolesAllowed("admin.timeslots:write")
    fun createUnavailableTimeslot(request: TimeslotCreateOrUpdateRequest): TimeslotResponse {
        val daySchedule = scheduleRepository.findByIdOrNull(request.date)
            ?: throw EntityNotFoundException("На этот день (${request.date}) записи (пока) нет")

        validateTimeIsAvailable(request.timeFrom, request.duration, daySchedule)

        return timeslotRepository.save(
            Timeslot(
                request.timeFrom,
                request.duration,
                daySchedule,
                TimeslotStatus.UNAVAILABLE
            )
        ).let { TimeslotResponse.Mapper.from(it) }
    }

    @Transactional
    @RolesAllowed("admin.timeslots:write")
    fun deleteUnavailableTimeslot(id: UUID) {
        val timeslot = timeslotRepository.findByIdOrThrow(id)
        if (timeslot.timeslotStatus != TimeslotStatus.UNAVAILABLE) throw BadRequestException(
            when (timeslot.timeslotStatus) {
                TimeslotStatus.BUSY -> "На данное время есть запись"
                TimeslotStatus.FREE -> "Данное время доступно для записи"
                else -> "Данное время не помечено как недоступное"
            }
        )
        timeslotRepository.delete(timeslot)
    }

    @Transactional
    @RolesAllowed("admin.timeslots:read")
    fun getUnavailableTimeslotsByDate(date: LocalDate) : List<TimeslotResponse> =
        timeslotRepository.findAllByDaySchedule(date)
            .filter { it.timeslotStatus == TimeslotStatus.UNAVAILABLE }
            .map { TimeslotResponse.Mapper.from(it)}

    @Transactional
    @RolesAllowed("admin.timeslots:read")
    fun getById(id: UUID) : TimeslotResponse =
        timeslotRepository.findByIdOrThrow(id)
            .let { TimeslotResponse.Mapper.from(it) }


    private fun validateTimeIsAvailable(timeFrom: LocalTime, duration: Duration, daySchedule: DaySchedule) {
        if (timeFrom < daySchedule.workingTimeFrom || timeFrom.plus(duration) > daySchedule.workingTimeTo) throw UnavailableActionException(
            "Выбранное время выходит за рамки рабочего времени"
        )

        if (daySchedule.bookedTimeslots.none { it.timeslotStatus == TimeslotStatus.BUSY }) return

        daySchedule.bookedTimeslots
            .filter { it.timeslotStatus == TimeslotStatus.BUSY }
            .forEach {
                if (timeFrom < it.timeFrom.plus(duration) && timeFrom.plus(duration) > it.timeFrom) throw UnavailableActionException(
                    "В это время уже есть занятые слоты, отмените или перенесите их сначала"
                )
            }
    }
}