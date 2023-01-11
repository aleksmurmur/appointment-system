package com.aleksmurmur.hairdresser.client.dto

import com.aleksmurmur.hairdresser.booking.dto.BookingResponse
import com.aleksmurmur.hairdresser.client.domain.Client
import java.util.UUID

class ClientResponse (
    val id: UUID,
    val phone: String,
    val name: String?,
    val bookings: List<BookingResponse> = listOf()
        ) {
object Mapper {
    fun from(client: Client) = ClientResponse(
        client.persistentId,
        client.phone,
        client.name,
        client.bookings.map { BookingResponse.Mapper.from(it) }
    )
}

}