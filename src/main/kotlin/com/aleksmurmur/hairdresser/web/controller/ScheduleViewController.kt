package com.aleksmurmur.hairdresser.web.controller

import com.aleksmurmur.hairdresser.api.SCHEDULE_VIEW_PATH
import com.aleksmurmur.hairdresser.schedule.service.ScheduleService
import com.aleksmurmur.hairdresser.web.dto.DayScheduleCreateOrUpdateForm
import com.aleksmurmur.hairdresser.web.dto.DayScheduleCreateOrUpdateForm.Mapper.toCreateRequest
import com.aleksmurmur.hairdresser.web.dto.DayScheduleCreateOrUpdateForm.Mapper.toForm
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.support.SessionStatus
import java.time.LocalDate

@Controller
@RequestMapping(SCHEDULE_VIEW_PATH)
class ScheduleViewController (
    private val scheduleService: ScheduleService
        ){

    @GetMapping("/new")
    fun initCreationForm(
        model: Model, request: HttpServletRequest
    ): String {
        model.addAttribute("scheduleForm", DayScheduleCreateOrUpdateForm())
        model.addAttribute("path", request.servletPath)
        model.addAttribute("action", "create")
        return "schedule/dayScheduleCreateOrUpdateForm"
    }

    @PostMapping("/new")
    fun processCreationForm(@[Valid ModelAttribute("scheduleForm")] form: DayScheduleCreateOrUpdateForm, result: BindingResult, session: SessionStatus): String {
        return if (result.hasErrors()) "schedule/dayScheduleCreateOrUpdateForm"
        else {
            val response = scheduleService.createDaySchedule(form.toCreateRequest())
            session.setComplete()
            "redirect:$SCHEDULE_VIEW_PATH/${response.date}"
        }
    }

    @GetMapping("/{date}")
    fun getById(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate, model: Model) : String {
        model.addAttribute(scheduleService.getOneDaySchedule(date))
        return "schedule/dayScheduleResponse"
    }

    @GetMapping("")
    fun getAll(@[RequestParam DateTimeFormat(iso = DateTimeFormat.ISO.DATE)] dateFrom: LocalDate?,
               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dateTo: LocalDate?,
               model: Model, request: HttpServletRequest): String {
        model.addAttribute("schedule", scheduleService.getScheduleByDates(
            dateFrom?: LocalDate.now(),
            dateTo?: LocalDate.now().plusDays(7) ))
        model.addAttribute("path", request.servletPath)
        return "schedule/scheduleList"
    }


    @GetMapping("/{date}/edit")
    fun initUpdateForm(@[PathVariable DateTimeFormat(iso = DateTimeFormat.ISO.DATE)] date: LocalDate, model: Model, request: HttpServletRequest): String {
        model.addAttribute("scheduleForm", scheduleService.getOneDaySchedule(date).toForm())
        model.addAttribute("path", request.servletPath)
        model.addAttribute("action", "update")
        return "schedule/dayScheduleCreateOrUpdateForm"
    }

    @PostMapping("/{date}/edit")
    fun processUpdateForm(@[PathVariable DateTimeFormat(iso = DateTimeFormat.ISO.DATE)] date: LocalDate,
                          @[Valid ModelAttribute("scheduleForm")] form: DayScheduleCreateOrUpdateForm, result: BindingResult, session: SessionStatus, model: Model) : String {
        return if (result.hasErrors()) "schedule/dayScheduleCreateOrUpdateForm"
        else {
            form.date = date
            scheduleService.updateDayScheduleByDate(form.toCreateRequest())
            session.setComplete()
            "redirect:${SCHEDULE_VIEW_PATH}/{date}"
        }
    }

    @PostMapping("/{date}/delete")
    fun deleteDaySchedule(@[PathVariable DateTimeFormat(iso = DateTimeFormat.ISO.DATE)] date: LocalDate): String {
        scheduleService.deleteDayScheduleByDate(date)
        return "redirect:${SCHEDULE_VIEW_PATH}"
    }

    //TODO timetable create and delete methods, htmls
//    @GetMapping("/new/timetable")
//    fun initTimetableCreationForm(
//        model: Model, request: HttpServletRequest
//    ): String {
//        model.addAttribute("timetableForm", TimetableCreateForm())
//        model.addAttribute("path", request.servletPath)
//        model.addAttribute("action", "create")
//        return "schedule/timetableCreateOrUpdateForm"
//    }
//
//    @PostMapping("/new/timetable")
//    fun processTimetableCreationForm(@[Valid ModelAttribute("scheduleForm")] form: TimetableCreateForm, result: BindingResult, session: SessionStatus): String {
//        return if (result.hasErrors()) "schedule/timetableCreateOrUpdateForm"
//        else {
//            val response = scheduleService.createTimetableSchedule(form.toCreateRequest())
//            session.setComplete()
//            val uri = UriComponentsBuilder
//                .fromUriString(SCHEDULE_VIEW_PATH)
//                .queryParam("dateFrom", form.dateFrom)
//                .queryParam("dateTo", form.dateTo)
//                .toUriString()
//            println(uri)
//            "redirect:${uri}"
//        }
//    }



//
//    @GetMapping("/{dateId}")
//    fun getOneDaySchedule(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dateId: LocalDate) : ResponseEntity<DayScheduleResponse> =
//        ResponseEntity.ok(
//            scheduleService.getOneDaySchedule(dateId)
//        )
//
//    @GetMapping
//    fun getByDate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dateFrom: LocalDate,
//                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dateTo: LocalDate
//    ) : ResponseEntity<List<DayScheduleResponse>> =
//        ResponseEntity.ok(
//            scheduleService.getScheduleByDates(dateFrom, dateTo)
//        )
//
//    @PostMapping
//    fun createDaySchedule(@[RequestBody Valid] request: DayScheduleCreateOrUpdateRequest) : ResponseEntity<DayScheduleResponse> =
//        ResponseEntity.ok(scheduleService.createDaySchedule(request))
//
//    @PostMapping("/timetable")
//    fun createTimetable(@[RequestBody Valid] request: TimetableCreateRequest) : ResponseEntity<List<DayScheduleResponse>> =
//        ResponseEntity.ok(scheduleService.createTimetableSchedule(request))
//
//    @PatchMapping
//    fun updateDaySchedule(@[RequestBody Valid] request: DayScheduleCreateOrUpdateRequest) : ResponseEntity<DayScheduleResponse> =
//        ResponseEntity.ok(scheduleService.updateDayScheduleByDate(request))
//
//    @DeleteMapping("/{dateId}")
//    fun deleteDaySchedule(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dateId: LocalDate) : ResponseEntity<Unit> =
//        scheduleService.deleteDayScheduleByDate(dateId)
//            .let { ResponseEntity.noContent().build() }
//
//    @DeleteMapping
//    fun deleteDaySchedule(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dateFrom: LocalDate,
//                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dateTo: LocalDate
//    ) : ResponseEntity<Unit> =
//        scheduleService.deleteScheduleBetweenDates(dateFrom, dateTo)
//            .let { ResponseEntity.noContent().build() }
//


}