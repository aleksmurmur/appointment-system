package com.aleksmurmur.hairdresser.client.dto

import com.aleksmurmur.hairdresser.client.domain.Client
import java.util.UUID

class ClientResponse (
    val id: UUID,
    val phone: String,
    val name: String?,
        ) {
object Mapper {
    fun from(client: Client) = ClientResponse(
        client.persistentId,
        client.phone,
        client.name,
    )
}

}