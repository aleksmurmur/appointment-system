package com.aleksmurmur.hairdresser.product.repository

import com.aleksmurmur.hairdresser.product.domain.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ProductRepository : JpaRepository<Product, UUID> {

    fun findByDeletedIsFalse() : List<Product>

    fun findByNameContainsIgnoreCaseAndDeletedIsFalse(name: String) : List<Product>
}