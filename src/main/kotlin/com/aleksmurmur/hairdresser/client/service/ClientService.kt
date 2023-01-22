package com.aleksmurmur.hairdresser.client.service

import com.aleksmurmur.hairdresser.client.domain.Client
import com.aleksmurmur.hairdresser.client.dto.ClientCreateOrUpdateRequest
import com.aleksmurmur.hairdresser.client.dto.ClientResponse
import com.aleksmurmur.hairdresser.client.repository.ClientRepository
import com.aleksmurmur.hairdresser.common.jpa.findByIdOrThrow
import jakarta.annotation.security.RolesAllowed
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ClientService (
    val clientRepository: ClientRepository
        ){

    companion object : KLogging()

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
    fun createClient(request: ClientCreateOrUpdateRequest): ClientResponse {
        return clientRepository.save(createEntity(request))
            .let {ClientResponse.Mapper.from(it)}
            .also { logger.debug { """
               Created client with id: ${it.id} 
            """ } }
    }

    @Transactional
    @RolesAllowed("admin.clients:write")
    fun update(id: UUID, request: ClientCreateOrUpdateRequest): ClientResponse {
        val entity = getClient(id)
            .also { it.updateFromRequest(request) }
        return clientRepository.save(entity).let { ClientResponse.Mapper.from(it) }
            .also { logger.debug { """
               Updated client with id: ${it.id} 
            """ } }
    }


    @Transactional
    @RolesAllowed("admin.clients:write")
    fun delete(id: UUID) =
        getClient(id)
            .apply { deleted = true }
            .also { logger.debug { """
               Deleted client with id: ${it.id} 
            """ } }

    private fun getClient(id: UUID) : Client =
        clientRepository.findByIdOrThrow(id)


    private fun createEntity(request: ClientCreateOrUpdateRequest) =
        Client(
            request.phone,
            request.name
        )

    private fun Client.updateFromRequest(request: ClientCreateOrUpdateRequest): Client {
         phone = request.phone
        name = request.name
        return this
    }
}