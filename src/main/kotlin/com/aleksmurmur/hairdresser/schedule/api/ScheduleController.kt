package com.aleksmurmur.hairdresser.schedule.api

import com.aleksmurmur.hairdresser.api.SCHEDULE_PATH
import com.aleksmurmur.hairdresser.schedule.dto.DayScheduleCreateOrUpdateRequest
import com.aleksmurmur.hairdresser.schedule.dto.DayScheduleResponse
import com.aleksmurmur.hairdresser.schedule.dto.TimetableCreateRequest
import com.aleksmurmur.hairdresser.schedule.service.ScheduleService
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping(SCHEDULE_PATH)
class ScheduleController (
    private val scheduleService: ScheduleService
        ){


    @GetMapping("/{dateId}")
    fun getOneDaySchedule(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dateId: LocalDate) : ResponseEntity<DayScheduleResponse> =
        ResponseEntity.ok(
            scheduleService.getOneDaySchedule(dateId)
        )

    @GetMapping
    fun getByDate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dateFrom: LocalDate,
                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dateTo: LocalDate) : ResponseEntity<List<DayScheduleResponse>> =
        ResponseEntity.ok(
            scheduleService.getScheduleByDates(dateFrom, dateTo)
        )

    @PostMapping
    fun createDaySchedule(@[RequestBody Valid] request: DayScheduleCreateOrUpdateRequest) : ResponseEntity<DayScheduleResponse> =
        ResponseEntity.ok(scheduleService.createDaySchedule(request))

    @PostMapping("/timetable")
    fun createTimetable(@[RequestBody Valid] request: TimetableCreateRequest) : ResponseEntity<List<DayScheduleResponse>> =
        ResponseEntity.ok(scheduleService.createTimetableSchedule(request))

    @PatchMapping
    fun updateDaySchedule(@[RequestBody Valid] request: DayScheduleCreateOrUpdateRequest) : ResponseEntity<DayScheduleResponse> =
        ResponseEntity.ok(scheduleService.updateDayScheduleByDate(request))

    @DeleteMapping("/{dateId}")
    fun deleteDaySchedule(@PathVariable  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dateId: LocalDate) : ResponseEntity<Unit> =
        scheduleService.deleteDayScheduleByDate(dateId)
            .let { ResponseEntity.noContent().build() }

    @DeleteMapping
    fun deleteDaySchedule(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dateFrom: LocalDate,
                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dateTo: LocalDate) : ResponseEntity<Unit> =
        scheduleService.deleteScheduleBetweenDates(dateFrom, dateTo)
            .let { ResponseEntity.noContent().build() }



}
