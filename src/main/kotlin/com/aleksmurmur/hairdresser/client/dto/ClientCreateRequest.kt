package com.aleksmurmur.hairdresser.client.dto

import jakarta.validation.constraints.NotBlank

data class ClientCreateRequest(
    @field:NotBlank
    val phone: String,
    val name: String?
) {
}