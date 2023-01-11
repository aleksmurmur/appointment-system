package com.aleksmurmur.hairdresser.product.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero

data class ProductCreateRequest(
    @field:NotBlank
    val name: String,
    val description: String?,
    @field:PositiveOrZero
    val price: Long,
    @field:Positive
    val durationMinutes: Long
) {
}