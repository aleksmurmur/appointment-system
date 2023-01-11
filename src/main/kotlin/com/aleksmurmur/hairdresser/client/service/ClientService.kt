package com.aleksmurmur.hairdresser.client.service

import com.aleksmurmur.hairdresser.client.domain.Client
import com.aleksmurmur.hairdresser.client.dto.ClientCreateRequest
import com.aleksmurmur.hairdresser.client.dto.ClientResponse
import com.aleksmurmur.hairdresser.client.dto.ClientUpdateRequest
import com.aleksmurmur.hairdresser.client.repository.ClientRepository
import com.aleksmurmur.hairdresser.common.jpa.findByIdOrThrow
import jakarta.annotation.security.RolesAllowed
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ClientService (
    val clientRepository: ClientRepository
        ){

    @Transactional(readOnly = true)
    @RolesAllowed("admin.clients:read")
    fun getByPhone(phone: String) : List<ClientResponse> =
        clientRepository.findByPhoneContains(phone)
            .map { ClientResponse.Mapper.from(it) }

    @Transactional(readOnly = true)
    @RolesAllowed("admin.clients:read")
    fun getAll() : List<ClientResponse> =
        clientRepository.findAll()
            .map { ClientResponse.Mapper.from(it) }

    @Transactional(readOnly = true)
    @RolesAllowed("admin.clients:read")
    fun getAllActive() : List<ClientResponse> =
        clientRepository.findByDeletedIsFalse()
            .map { ClientResponse.Mapper.from(it) }

    @Transactional(readOnly = true)
    @RolesAllowed("admin.clients:read")
    fun getById(id: UUID) =
        getClient(id)
            .let { ClientResponse.Mapper.from(it) }

    @Transactional
    @RolesAllowed("admin.clients:write")
    fun createClient(request: ClientCreateRequest): ClientResponse {
        return clientRepository.save(createEntity(request))
            .let {ClientResponse.Mapper.from(it)}
    }

    @Transactional
    @RolesAllowed("admin.clients:write")
    fun update(id: UUID, request: ClientUpdateRequest): ClientResponse {
        val entity = getClient(id)
            .also { it.updateFromRequest(request) }
        return clientRepository.save(entity).let { ClientResponse.Mapper.from(it) }
    }


    @Transactional
    @RolesAllowed("admin.clients:write")
    fun delete(id: UUID) =
        getClient(id)
            .apply { deleted = true }

    private fun getClient(id: UUID) : Client =
        clientRepository.findByIdOrThrow(id)


    private fun createEntity(request: ClientCreateRequest) =
        Client(
            request.phone,
            request.name
        )

    private fun Client.updateFromRequest(request: ClientUpdateRequest): Client {
        request.phone?.let { phone = it }
        request.name?.let { name = it }
        return this
    }
}