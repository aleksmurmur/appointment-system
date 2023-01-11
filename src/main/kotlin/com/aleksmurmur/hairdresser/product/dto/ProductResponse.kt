package com.aleksmurmur.hairdresser.product.dto

import com.aleksmurmur.hairdresser.product.domain.Product
import java.util.UUID

class ProductResponse (
    val id: UUID,
    val name: String,
    val description: String?,
    val price: Long,
    val durationMinutes: Long,
    val deleted: Boolean
        ){
    object Mapper {
        fun from(p: Product) = ProductResponse(
            p.persistentId,
            p.name,
            p.description,
            p.price,
            p.duration.toMinutes(),
            p.deleted
        )
    }
}