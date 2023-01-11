package com.aleksmurmur.hairdresser.tests

import com.aleksmurmur.hairdresser.api.CLIENTS_PATH
import com.aleksmurmur.hairdresser.client.domain.Client
import com.aleksmurmur.hairdresser.client.dto.ClientCreateRequest
import com.aleksmurmur.hairdresser.client.dto.ClientResponse
import com.aleksmurmur.hairdresser.client.dto.ClientUpdateRequest
import com.aleksmurmur.hairdresser.configuration.Context
import com.example.client
import com.example.clientCreateRequest
import com.example.clientUpdateRequest
import com.example.response
import com.fasterxml.jackson.core.type.TypeReference
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.util.LinkedMultiValueMap
import java.util.UUID

class ClientApiTests : Context() {

    lateinit var client1: Client
    lateinit var client2: Client
    lateinit var clientWithPhone: Client
    lateinit var clientWithSamePartOfPhone: Client


    @BeforeEach
    fun beforeAll() {
        cleanDB()
        clientRepository.save(client()).run { client1 = this }
        clientRepository.save(client()).run { client2 = this }
        clientRepository.save(client("111888800")).run { clientWithPhone = this }
        clientRepository.save(client("388889")).run { clientWithSamePartOfPhone = this }
    }

    @Nested
    inner class GetById {

        @Test
        fun `returns client by id`() {
            getClient(client1.persistentId)
                .andExpect { status { isOk() } }
                .response<ClientResponse>(mapper)
                .run {
                    assertEquals(client1.persistentId, id)
                    assertEquals(client1.phone, phone)
                    assertEquals(client1.name, name)
                }
        }

        @Test
        fun `returns 404 if not found`() {
            getClient(UUID.randomUUID())
                .andExpect { status { isNotFound() } }
        }

        private fun getClient(id: UUID) =
            testClient.get("$CLIENTS_PATH/$id")
    }

    @Nested
    inner class GetAll {
        @Test
        fun `returns all clients`() {
            getClients()
                .andExpect { status { isOk() } }
                .response(mapper, object : TypeReference<List<ClientResponse>>() {})
                .run {
                    assertEquals(4, size)
                    assertEquals(client1.persistentId, first().id)
                    assertEquals(client1.phone, first().phone)
                    assertEquals(client1.name, first().name)
                }
        }

        @Test
        fun `returns client by phone`() {
            getClients(client2.phone)
                .andExpect { status { isOk() } }
                .response(mapper, object : TypeReference<List<ClientResponse>>() {})
                .run {
                    assertEquals(1, size)
                    assertEquals(client2.persistentId, first().id)
                    assertEquals(client2.phone, first().phone)
                    assertEquals(client2.name, first().name)
                }
        }

        @Test
        fun `returns empty list if not found by phone`() {
            getClients("9991234567")
                .andExpect { status { isOk() } }
                .response(mapper, object : TypeReference<List<ClientResponse>>() {})
                .run {
                    assert(isEmpty())
                }
        }

        @Test
        fun `returns clients by phone contains`() {
        getClients("8888")
            .andExpect { status { isOk() } }
            .response(mapper, object : TypeReference<List<ClientResponse>>() {})
            .run {
                assertEquals(2, size)
                assertEquals(clientWithPhone.name, first().name)
            }
        }


        private fun getClients(phone: String? = null) =
            testClient.get(CLIENTS_PATH) {
                params = LinkedMultiValueMap<String, String>()
                    .apply {
                        setAll(
                            mapOf("phone" to phone)
                                .filter { it.value != null })
                    }
            }
    }

    @Nested
    inner class Delete {
        @Test
        fun `returns no content after deleting`() {
            deleteClient(client1.persistentId)
                .andExpect { status { isNoContent() } }

            getActiveClients()
                .andExpect { status { isOk() } }
                .response(mapper, object : TypeReference<List<ClientResponse>>() {})
                .run {
                    assertEquals(3, size)
                }
        }

        @Test
        fun `returns 404 if not found`() {
            deleteClient(UUID.randomUUID())
                .andExpect { status { isNotFound() } }
        }

        private fun deleteClient(id: UUID) =
            testClient.delete("$CLIENTS_PATH/$id")
        private fun getActiveClients() =
            testClient.get(CLIENTS_PATH)
    }

    @Nested
    inner class Create {
        @Test
        fun `successfully creates client`() {
            val request = clientCreateRequest()
            createClient(request)
                .andExpect { status { isOk() } }
                .response<ClientResponse>(mapper)
                .run {
                    assertEquals(request.phone, phone)
                    assertEquals(request.name, name)
                }
        }
        @Test
        fun `successfully creates client without name`() {
            val request = clientCreateRequest().copy(name = null)
            createClient(request)
                .andExpect { status { isOk() } }
                .response<ClientResponse>(mapper)
                .run {
                    assertEquals(request.phone, phone)
                    assertEquals(null, name)
                }
        }

        @Test
        fun `returns bad request if phone is empty`()
        {
            val request = clientCreateRequest().copy(phone = "")
            createClient(request)
                .andExpect { status { isBadRequest() } }
        }

        private fun createClient(request: ClientCreateRequest) =
            testClient.post(CLIENTS_PATH) {
                content = mapper.writeValueAsString(request)
                contentType = MediaType.APPLICATION_JSON
            }
    }

    @Nested
    inner class Update {
        @Test
        fun `updates client`() {
            val request = clientUpdateRequest()
        updateClient(request, client1.persistentId)
            .andExpect { status { isOk() } }
            .response<ClientResponse>(mapper)
            .run {
                assertEquals(request.name, name)
                assertEquals(request.phone, phone)
            }
        }

        @Test
        fun `updates only phone`() {
            val request = clientUpdateRequest().copy(name = null)
            updateClient(request, client1.persistentId)
                .andExpect { status { isOk() } }
                .response<ClientResponse>(mapper)
                .run {
                    assertEquals(client1.name, name)
                    assertEquals(request.phone, phone)
                }
        }

        @Test
        fun `updates only name`() {
            val request = clientUpdateRequest().copy(phone = null)
            updateClient(request, client1.persistentId)
                .andExpect { status { isOk() } }
                .response<ClientResponse>(mapper)
                .run {
                    assertEquals(request.name, name)
                    assertEquals(client1.phone, phone)
                }
        }

        @Test
        fun `returns 404 if non existent id handled`() {
            updateClient(clientUpdateRequest(), UUID.randomUUID())
                .andExpect { status { isBadRequest() } }
        }

        private fun updateClient(request: ClientUpdateRequest, clientId: UUID) =
            testClient
                .patch("$CLIENTS_PATH/$clientId") {
                    content = mapper.writeValueAsString(request)
                    contentType = MediaType.APPLICATION_JSON
                }
    }
}
