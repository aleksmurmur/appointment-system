package com.aleksmurmur.hairdresser.web.dto

import com.aleksmurmur.hairdresser.client.dto.ClientCreateOrUpdateRequest
import com.aleksmurmur.hairdresser.client.dto.ClientResponse
import jakarta.validation.constraints.NotBlank

class ClientCreateOrUpdateForm (
    @field:NotBlank
    var phone: String? = null,
    var name: String? = null
        ) {

    companion object Mapper {
        fun ClientCreateOrUpdateForm.toCreateRequest() = ClientCreateOrUpdateRequest(
            phone!!,
            name
        )

        fun ClientResponse.toForm() = ClientCreateOrUpdateForm(
            phone,
            name
        )
    }
}