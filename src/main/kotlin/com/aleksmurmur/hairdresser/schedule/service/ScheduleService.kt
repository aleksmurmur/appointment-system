package com.aleksmurmur.hairdresser.schedule.service

import com.aleksmurmur.hairdresser.common.jpa.findByIdOrThrow
import com.aleksmurmur.hairdresser.exception.BadRequestException
import com.aleksmurmur.hairdresser.exception.EntityNotFoundException
import com.aleksmurmur.hairdresser.exception.UnavailableActionException
import com.aleksmurmur.hairdresser.schedule.domain.DaySchedule
import com.aleksmurmur.hairdresser.booking.domain.Timeslot
import com.aleksmurmur.hairdresser.booking.domain.TimeslotStatus
import com.aleksmurmur.hairdresser.schedule.dto.DayScheduleCreateOrUpdateRequest
import com.aleksmurmur.hairdresser.schedule.dto.DayScheduleResponse
import com.aleksmurmur.hairdresser.booking.dto.TimeslotResponse
import com.aleksmurmur.hairdresser.schedule.dto.TimetableCreateRequest
import com.aleksmurmur.hairdresser.schedule.repository.ScheduleRepository
import jakarta.annotation.security.RolesAllowed
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.TimeUnit

@Service
class ScheduleService(
    val scheduleRepository: ScheduleRepository,
    @Value("\${app.service.shortest}") val comparator: Long
) {

    @Transactional(readOnly = true)
    @RolesAllowed("admin.schedule:read")
    fun getOneDaySchedule(date: LocalDate): DayScheduleResponse {
        val daySchedule =
            scheduleRepository.findByIdOrNull(date) ?: throw EntityNotFoundException("На этот день (${date}) записи (пока) нет")

        return daySchedule.mapToDto()

    }

    @Transactional(readOnly = true)
    @RolesAllowed("admin.schedule:read")
    fun getScheduleByDates(dateFrom: LocalDate, dateTo: LocalDate): List<DayScheduleResponse> =
        scheduleRepository.findByIdGreaterThanEqualAndIdLessThanEqual(dateFrom, dateTo)
            .sortedBy { it.persistentId }
            .map { it.mapToDto() }

    @Transactional
    @RolesAllowed("admin.schedule:write")
    fun createDaySchedule(request: DayScheduleCreateOrUpdateRequest): DayScheduleResponse {
        throwExceptionIfScheduleExists(request.date)
        return scheduleRepository.save(createEntity(request)).mapToDto()
    }

    @Transactional
    @RolesAllowed("admin.schedule:write")
    fun createTimetableSchedule(request: TimetableCreateRequest) : List<DayScheduleResponse> {
        if (scheduleRepository
                .findByIdGreaterThanEqualAndIdLessThanEqual(request.dateFrom, request.dateTo)
                .isNotEmpty()
        ) throw UnavailableActionException("В указанном промежутке уже есть заполненные расписания. Удалите их или выберите другие даты")


        val result = mutableListOf<DayScheduleResponse>()

         request.dateFrom
             .datesUntil(request.dateTo)
             .forEach {currentDate ->
                 request.workingSchedule[currentDate.dayOfWeek]
                     .run { if (this != null )
                         scheduleRepository.save(
                             DaySchedule(
                                 currentDate,
                                 this.timeFrom,
                                 this.timeTo,
                                 this.workingDay
                                 )
                                 .also { result.add(it.mapToDto()) }
                         )
                     }
             }
        return result
    }

    @Transactional
    @RolesAllowed("admin.schedule:write")
    fun deleteDayScheduleByDate(date: LocalDate) {
        val daySchedule = scheduleRepository.findByIdOrThrow(date)
        if (daySchedule.bookedTimeslots.any { it.timeslotStatus == TimeslotStatus.BUSY }) throw UnavailableActionException("На $date уже есть записи, отмените или перенесите их")
        return scheduleRepository.delete(daySchedule)
    }

    @Transactional
    @RolesAllowed("admin.schedule:write")
    fun deleteScheduleBetweenDates(dateFrom: LocalDate, dateTo: LocalDate) =
        scheduleRepository.findByIdGreaterThanEqualAndIdLessThanEqual(dateFrom, dateTo)
            .onEach { daySchedule ->
                if (daySchedule.bookedTimeslots.any { it.timeslotStatus == TimeslotStatus.BUSY }) throw UnavailableActionException("На ${daySchedule.persistentId} уже есть записи, отмените или перенесите их")
            }
            .forEach {
                scheduleRepository.delete(it)
            }


    @Transactional
    @RolesAllowed("admin.schedule:write")
    fun updateDayScheduleByDate(request: DayScheduleCreateOrUpdateRequest): DayScheduleResponse {
        val daySchedule =
            scheduleRepository.findByIdOrThrow(request.date)

        val bookedTimeslots = daySchedule.bookedTimeslots.filter { it.timeslotStatus == TimeslotStatus.BUSY }

        if (bookedTimeslots.isNotEmpty()) {
            if (!request.workingDay) throw UnavailableActionException("На указанную дату уже есть записи, отмените или перенесите их")

            bookedTimeslots
                .sortedBy { it.timeFrom }
                .let {
                    request.checkTimeConflicts(it.first().timeFrom, it.last().run { this.timeFrom.plus(this.duration) })
                }
        }

        return scheduleRepository.save(daySchedule.updateEntity(request)).mapToDto()

    }



    private fun DaySchedule.updateEntity(request: DayScheduleCreateOrUpdateRequest): DaySchedule {
        request.timeFrom?.let { this.workingTimeFrom = it }
        request.timeTo?.let { this.workingTimeTo = it }
        this.workingDay = request.workingDay
        return this
    }

    private fun DayScheduleCreateOrUpdateRequest.checkTimeConflicts(
        bookedTimeFrom: LocalTime,
        bookedTimeTo: LocalTime
    ) {
        if (timeFrom != null && timeFrom > bookedTimeFrom) throw UnavailableActionException("Начало рабочего дня (${timeFrom}) не может быть позже первой записи (${bookedTimeFrom})")
        if (timeTo != null && timeTo < bookedTimeTo) throw UnavailableActionException("Конец рабочего дня (${timeTo}) не может быть раньше последней записи (${bookedTimeTo})")
    }

    private fun createEntity(request: DayScheduleCreateOrUpdateRequest) = DaySchedule(
        date = request.date,
        workingTimeFrom = request.timeFrom,
        workingTimeTo = request.timeTo,
        workingDay = request.workingDay
    )

    private fun throwExceptionIfScheduleExists(date: LocalDate) {
        scheduleRepository.findByIdOrNull(date)?.let {
            throw BadRequestException("На этот день уже есть расписание, обновите его")
        }

    }

    private fun DaySchedule.mapToDto() = DayScheduleResponse(
        date = this.persistentId,
        workingTimeFrom = this.workingTimeFrom,
        workingTimeTo = this.workingTimeTo,
        timeslots = addTimeslots(this),
        workingDay = this.workingDay
    )

    private fun addTimeslots(schedule: DaySchedule): MutableList<TimeslotResponse> {
        if (!schedule.workingDay) return mutableListOf()

        val bookedTimeslots = schedule.bookedTimeslots.filter { it.timeslotStatus == TimeslotStatus.BUSY }

        if (bookedTimeslots.isEmpty()) return mutableListOf(
            TimeslotResponse(
                timeFrom = schedule.workingTimeFrom!!,
                timeTo = schedule.workingTimeTo!!,
                status = TimeslotStatus.FREE
            )
        )

        return bookedTimeslots
            .filter { it.timeslotStatus == TimeslotStatus.BUSY }
            .sortedBy { it.timeFrom }
            .addFreeTimeslots(schedule.workingTimeFrom!!, schedule.workingTimeTo!!)

    }

    private fun List<Timeslot>.addFreeTimeslots(
        timeFrom: LocalTime,
        timeTo: LocalTime
    ): MutableList<TimeslotResponse> {
        val result = mutableListOf<TimeslotResponse>()
        if (durationIsLongerThanShortestService(timeFrom, this.first().timeFrom)) result.add(
            TimeslotResponse(
                timeFrom,
                this.first().timeFrom,
                TimeslotStatus.FREE
            )
        )

        this.iterator()
            .withIndex()
            .forEach {
                result.add(TimeslotResponse.Mapper.from(it.value))
                if (it.index + 1 < this.size) {
                    val nextTimeFrom = this[it.index + 1].timeFrom
                    val thisTimeTo = it.value.timeFrom.plus(it.value.duration)
                    if (durationIsLongerThanShortestService(thisTimeTo, nextTimeFrom)) {
                        result.add(
                            TimeslotResponse(
                                thisTimeTo,
                                nextTimeFrom,
                                TimeslotStatus.FREE
                            )
                        )
                    }
                }
            }

        val lastTimeTo = this.last().timeFrom.plus(this.last().duration)
        if (durationIsLongerThanShortestService(
                lastTimeTo,
                timeTo
            )
        ) result.add(TimeslotResponse(lastTimeTo, timeTo, TimeslotStatus.FREE))

        return result
    }

    private fun durationIsLongerThanShortestService(timeFrom: LocalTime, timeTo: LocalTime): Boolean =
        Duration.between(timeFrom, timeTo).toMinutes() > TimeUnit.MINUTES.toMinutes(comparator)
}