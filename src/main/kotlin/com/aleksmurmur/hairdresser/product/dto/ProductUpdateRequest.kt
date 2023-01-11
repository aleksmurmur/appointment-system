package com.aleksmurmur.hairdresser.product.dto


data class ProductUpdateRequest (
    val name: String?,
    val description: String?,
    val price: Long?,
    val durationMinutes: Long?
        ) {
}