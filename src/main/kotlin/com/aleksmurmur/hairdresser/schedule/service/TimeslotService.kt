package com.aleksmurmur.hairdresser.schedule.service

import com.aleksmurmur.hairdresser.common.jpa.findByIdOrThrow
import com.aleksmurmur.hairdresser.exception.EntityNotFoundException
import com.aleksmurmur.hairdresser.schedule.domain.DaySchedule
import com.aleksmurmur.hairdresser.schedule.domain.Timeslot
import com.aleksmurmur.hairdresser.schedule.dto.TimeslotCreateOrUpdateRequest
import com.aleksmurmur.hairdresser.schedule.dto.TimeslotResponse
import com.aleksmurmur.hairdresser.schedule.repository.ScheduleRepository
import com.aleksmurmur.hairdresser.schedule.repository.TimeslotRepository
import jakarta.annotation.security.RolesAllowed
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class TimeslotService (
    val timeslotRepository: TimeslotRepository,
    val scheduleRepository: ScheduleRepository
        ){


    @Transactional(readOnly = true)
    @RolesAllowed("admin.timeslots:read")
    fun getById(id: UUID) : TimeslotResponse =
        timeslotRepository.findByIdOrThrow(id)
            .let { TimeslotResponse.Mapper.from(it) }

    @Transactional(readOnly = true)
    @RolesAllowed("admin.timeslots:read")
    fun getAll() : List<TimeslotResponse> =
        timeslotRepository.findAll()
            .map { TimeslotResponse.Mapper.from(it) }

    @Transactional
    @RolesAllowed("admin.timeslots:write")
    fun create(request: TimeslotCreateOrUpdateRequest) : TimeslotResponse {
        val daySchedule = scheduleRepository.findByIdOrNull(request.dayScheduleId) ?: throw EntityNotFoundException("На этот день (${request.dayScheduleId}) записи (пока) нет")
        //Todo validate that time is free
        return timeslotRepository.save(request.toEntity(daySchedule)).let { TimeslotResponse.Mapper.from(it) }
    }

    @Transactional
    @RolesAllowed("admin.timeslots:write")
    fun update(id: UUID, request: TimeslotCreateOrUpdateRequest) : TimeslotResponse =
        //Todo validate that time is free
    timeslotRepository
        .findByIdOrThrow(id)
        .updateEntity(request)
        .let {
            TimeslotResponse.Mapper.from(it)
        }

    @Transactional
    @RolesAllowed("admin.timeslots:write")
    fun delete(id: UUID)  {
        val timeslot = timeslotRepository.findByIdOrThrow(id)
        //todo check if deletes in schedule and validate to delete in client
        return timeslotRepository.delete(timeslot)
    }

    private fun Timeslot.updateEntity(request: TimeslotCreateOrUpdateRequest) : Timeslot{
        timeFrom = request.timeFrom
        timeTo = request.timeTo
        if (daySchedule.persistentId != request.dayScheduleId) daySchedule = scheduleRepository.findByIdOrThrow(request.dayScheduleId)
    //todo check that timeslot deletes in prev schedule
        status = request.status
        return timeslotRepository.save(this)
    }




    private fun TimeslotCreateOrUpdateRequest.toEntity(daySchedule: DaySchedule) = Timeslot(
        timeFrom,
        timeTo,
        daySchedule,
        status
    )





}