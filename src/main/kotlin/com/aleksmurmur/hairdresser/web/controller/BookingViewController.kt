package com.aleksmurmur.hairdresser.web.controller

import com.aleksmurmur.hairdresser.api.BOOKINGS_VIEW_PATH
import com.aleksmurmur.hairdresser.api.SCHEDULE_VIEW_PATH
import com.aleksmurmur.hairdresser.booking.service.BookingService
import com.aleksmurmur.hairdresser.client.service.ClientService
import com.aleksmurmur.hairdresser.product.service.ProductService
import com.aleksmurmur.hairdresser.schedule.service.ScheduleService
import com.aleksmurmur.hairdresser.web.dto.BookingCreateForm
import com.aleksmurmur.hairdresser.web.dto.BookingCreateForm.Mapper.toCreateRequest
import com.aleksmurmur.hairdresser.web.dto.BookingTimeAndProductsUpdateForm
import com.aleksmurmur.hairdresser.web.dto.BookingTimeAndProductsUpdateForm.Mapper.toForm
import com.aleksmurmur.hairdresser.web.dto.BookingTimeAndProductsUpdateForm.Mapper.toUpdateRequest
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.support.SessionStatus
import java.time.LocalDate
import java.util.*

@Controller
@RequestMapping(BOOKINGS_VIEW_PATH)
class BookingViewController (
    private val bookingService: BookingService,
    private val clientService: ClientService,
    private val productService: ProductService,
    private val scheduleService: ScheduleService
        ){


    @GetMapping("/new")
    fun initCreationForm(
        model: Model, request: HttpServletRequest
    ): String {
        model.addAttribute("bookingForm", BookingCreateForm())
        model.addAttribute("schedule", scheduleService.getScheduleByDates(LocalDate.now(), LocalDate.now().plusDays(7)))
        model.addAttribute("clients", clientService.getAllActive())
        model.addAttribute("products", productService.getAll())
        model.addAttribute("path", request.servletPath)
        return "bookings/bookingCreateForm"
    }

    @PostMapping("/new")
    fun preprocessCreationForm(form: BookingCreateForm, result: BindingResult, session: SessionStatus, model: Model, request: HttpServletRequest): String {
        return if (result.hasErrors()) "bookings/bookingCreateForm"
        else {

            model.addAttribute("schedule", scheduleService.getScheduleByDates(LocalDate.now(), LocalDate.now().plusDays(7)))
            model.addAttribute("clients", clientService.getAllActive())
            model.addAttribute("products", productService.getAll())
            model.addAttribute("path", request.servletPath.plus("/create"))
            model.addAttribute("time", scheduleService.getSuitableTimeslots(form.date!!, form.products ))
            model.addAttribute("bookingForm", form)
            "bookings/bookingCreateForm"
        }
    }

    @PostMapping("/new/create")
    fun processCreationForm(@[Valid ModelAttribute("bookingForm")] form: BookingCreateForm, result: BindingResult, session: SessionStatus, model: Model): String {
        return if (result.hasErrors()) "bookings/bookingCreateForm"
        else {
            val response = bookingService.createBooking(form.toCreateRequest())
            session.setComplete()
            "redirect:$BOOKINGS_VIEW_PATH/${response.id}"
        }
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID, model: Model) : String {
        model.addAttribute(bookingService.getById(id))
        return "bookings/bookingResponse"
    }



    @GetMapping("")
    fun getAll(@RequestParam clientId: UUID?, model: Model, request: HttpServletRequest): String {
        val bookings = if (clientId == null) bookingService.getAllActive() else bookingService.getAllActiveByClient(clientId)
        model.addAttribute("clients", clientService.getAllActive())
        model.addAttribute("bookings", bookings)
        model.addAttribute("path", request.servletPath)
        model.addAttribute("schedulePath", SCHEDULE_VIEW_PATH)
        return "bookings/bookingsList"
    }

    @GetMapping("/{id}/edit")
    fun initUpdateForm(@PathVariable id: UUID, model: Model, request: HttpServletRequest): String {
        model.addAttribute("bookingUpdateForm", bookingService.getById(id).toForm())
        model.addAttribute("products", productService.getAll())
        model.addAttribute("path", request.servletPath)
        model.addAttribute("action", "update")
        return "bookings/bookingTimeAndProductsUpdateForm"
    }

    @PostMapping("/{id}/edit")
    fun processUpdateForm(@PathVariable id: UUID, @[ModelAttribute("bookingUpdateForm")] form: BookingTimeAndProductsUpdateForm, result: BindingResult, session: SessionStatus, model: Model) : String {
//        return if (result.hasErrors()) "bookings/bookingTimeAndProductsUpdateForm"
//        else {
            bookingService.updateBookingTimeAndProducts(id, form.toUpdateRequest())
            session.setComplete()
            return "redirect:$BOOKINGS_VIEW_PATH/{id}"
//        }
    }

    @PostMapping("/{id}/cancel")
    fun cancelBooking(@PathVariable id: UUID): String {
        bookingService.cancelBooking(id)
        return "redirect:$BOOKINGS_VIEW_PATH"
    }
}