package com.aleksmurmur.hairdresser.web.dto

import com.aleksmurmur.hairdresser.product.dto.ProductCreateRequest
import com.aleksmurmur.hairdresser.product.dto.ProductResponse
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero


class ProductCreateOrUpdateForm(
    @field:NotBlank
    var name: String? = null,
    var description: String? = null,
    @field:[PositiveOrZero NotNull]
    var price: Long? = null,
    @field:Positive
    @field:NotNull
    var durationMinutes: Long? = null
) {


    companion object Mapper {
        fun ProductCreateOrUpdateForm.toCreateRequest() = ProductCreateRequest(
            this.name!!,
            this.description,
            this.price!!,
            this.durationMinutes!!
        )

        fun ProductResponse.toForm() = ProductCreateOrUpdateForm(
            this.name,
            this.description,
            this.price,
            this.durationMinutes
        )
    }

}