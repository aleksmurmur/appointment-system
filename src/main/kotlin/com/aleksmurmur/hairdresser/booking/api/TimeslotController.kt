package com.aleksmurmur.hairdresser.booking.api

import com.aleksmurmur.hairdresser.api.TIMESLOTS_PATH
import com.aleksmurmur.hairdresser.booking.dto.TimeslotCreateOrUpdateRequest
import com.aleksmurmur.hairdresser.booking.dto.TimeslotResponse
import com.aleksmurmur.hairdresser.booking.service.TimeslotService
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping(TIMESLOTS_PATH)
class TimeslotController (
    private val timeslotService: TimeslotService
        ){

    @GetMapping("/{id}")
    fun getTimeslot(@PathVariable id: UUID) : ResponseEntity<TimeslotResponse> =
        timeslotService.getById(id)
            .let { ResponseEntity.ok(it) }

    @GetMapping
    fun getUnavailableTimeslotsByDate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate) : ResponseEntity<List<TimeslotResponse>> =
        timeslotService.getUnavailableTimeslotsByDate(date)
            .let { ResponseEntity.ok(it) }

    @PostMapping
    fun createUnavailableTimeslot(@RequestBody @Valid request: TimeslotCreateOrUpdateRequest) : ResponseEntity<TimeslotResponse> =
        timeslotService.createUnavailableTimeslot(request)
            .let { ResponseEntity.ok(it) }

    @DeleteMapping("/{id}")
    fun deleteUnavailableTimeslot(@PathVariable id: UUID) : ResponseEntity<Unit> =
        timeslotService.deleteUnavailableTimeslot(id)
            .let { ResponseEntity.noContent().build() }


}