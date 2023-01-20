package com.aleksmurmur.hairdresser.client.api

import com.aleksmurmur.hairdresser.api.CLIENTS_PATH
import com.aleksmurmur.hairdresser.client.dto.ClientCreateOrUpdateRequest
import com.aleksmurmur.hairdresser.client.dto.ClientResponse
import com.aleksmurmur.hairdresser.client.service.ClientService
import jakarta.validation.Valid
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
import java.util.UUID


@RestController
@RequestMapping(CLIENTS_PATH)
class ClientController(
    private val clientService: ClientService
) {

    @PostMapping
    fun createClient(@RequestBody @Valid request: ClientCreateOrUpdateRequest): ResponseEntity<ClientResponse> =
        ResponseEntity.ok(clientService.createClient(request))

    @PatchMapping("/{id}")
    fun updateClient(
        @PathVariable id: UUID,
        @RequestBody request: ClientCreateOrUpdateRequest
    ): ResponseEntity<ClientResponse> =
        ResponseEntity.ok(clientService.update(id, request))

    @GetMapping("/{id}")
    fun getClient(@PathVariable id: UUID): ResponseEntity<ClientResponse> =
        ResponseEntity.ok(clientService.getById(id))

    @GetMapping
    fun getAllByPhone(@RequestParam(required = false) phone: String?) : ResponseEntity<List<ClientResponse>> =
        if (phone != null) clientService.getByPhone(phone).let { ResponseEntity.ok(it) }
        else clientService.getAllActive().let { ResponseEntity.ok(it) }

    @DeleteMapping("/{id}")
    fun deleteClient(@PathVariable id: UUID) : ResponseEntity<Unit> =
        clientService.delete(id)
            .let { ResponseEntity.noContent().build() }

}