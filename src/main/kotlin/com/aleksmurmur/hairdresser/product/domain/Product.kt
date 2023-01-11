package com.aleksmurmur.hairdresser.product.domain

import com.aleksmurmur.hairdresser.common.jpa.UUIDIdentifiableEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.Duration

@Entity
@Table (name = "products")
class  Product (
    var name: String,
    var description: String? = null,
    var price: Long,
    var duration: Duration,
    var deleted: Boolean = false
        ): UUIDIdentifiableEntity()