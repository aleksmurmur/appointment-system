package com.aleksmurmur.hairdresser.web.controller

import com.aleksmurmur.hairdresser.api.CLIENTS_VIEW_PATH
import com.aleksmurmur.hairdresser.client.service.ClientService
import com.aleksmurmur.hairdresser.web.dto.ClientCreateOrUpdateForm
import com.aleksmurmur.hairdresser.web.dto.ClientCreateOrUpdateForm.Mapper.toCreateRequest
import com.aleksmurmur.hairdresser.web.dto.ClientCreateOrUpdateForm.Mapper.toForm
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.support.SessionStatus
import java.util.*

@Controller
class ClientViewController   (
    private val clientService: ClientService
) {



    @GetMapping("/new")
    fun initCreationForm(
        model: Model, request: HttpServletRequest
    ): String {
        model.addAttribute("clientForm", ClientCreateOrUpdateForm())
        model.addAttribute("path", request.servletPath)
        model.addAttribute("action", "create")
        return "clients/clientCreateOrUpdateForm"
    }

    @PostMapping("/new")
    fun processCreationForm(@[Valid ModelAttribute("clientForm")] form: ClientCreateOrUpdateForm, result: BindingResult, session: SessionStatus, model: Model): String {
        return if (result.hasErrors()) "clients/clientCreateOrUpdateForm"
        else {
            val response = clientService.createClient(form.toCreateRequest())
            session.setComplete()
            "redirect:$CLIENTS_VIEW_PATH/${response.id}"
        }
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID, model: Model) : String {
        model.addAttribute(clientService.getById(id))
        return "products/productResponse"
    }

    @GetMapping("")
    fun getAll(@RequestParam phone: String?, model: Model, request: HttpServletRequest): String {
        val clients = if (phone == null) clientService.getAllActive() else clientService.getByPhone(phone)
        model.addAttribute("clients", clients)
        model.addAttribute("path", request.servletPath)
        return "clients/clientsList"
    }

    @GetMapping("/{id}/edit")
    fun initUpdateForm(@PathVariable id: UUID, model: Model, request: HttpServletRequest): String {
        model.addAttribute("clientForm", clientService.getById(id).toForm())
        model.addAttribute("path", request.servletPath)
        model.addAttribute("action", "update")
        return "clients/clientCreateOrUpdateForm"
    }

    @PostMapping("/{id}/edit")
    fun processUpdateForm(@PathVariable id: UUID, @[Valid ModelAttribute("clientForm")] form: ClientCreateOrUpdateForm, result: BindingResult, session: SessionStatus, model: Model) : String {
        return if (result.hasErrors()) "clients/clientCreateOrUpdateForm"
        else {
            clientService.update(id, form.toCreateRequest())
            session.setComplete()
            "redirect:$CLIENTS_VIEW_PATH/{id}"
        }
    }

    @PostMapping("/{id}/delete")
    fun deleteClient(@PathVariable id: UUID): String {
        clientService.delete(id)
        return "redirect:$CLIENTS_VIEW_PATH"
    }

}