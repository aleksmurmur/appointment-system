package com.aleksmurmur.hairdresser.product.service

import com.aleksmurmur.hairdresser.common.jpa.findByIdOrThrow
import com.aleksmurmur.hairdresser.product.domain.Product
import com.aleksmurmur.hairdresser.product.dto.ProductCreateRequest
import com.aleksmurmur.hairdresser.product.dto.ProductResponse

import com.aleksmurmur.hairdresser.product.repository.ProductRepository
import jakarta.annotation.security.RolesAllowed
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.util.UUID

@Service
class ProductService (
    val productRepository: ProductRepository
        ){

    @Transactional(readOnly = true)
    @RolesAllowed("admin.products:read")
    fun getAll() : List<ProductResponse> =
        productRepository.findByDeletedIsFalse()
            .map { ProductResponse.Mapper.from(it) }

    @Transactional(readOnly = true)
    @RolesAllowed("admin.products:read")
    fun getByNameLike(name: String) : List<ProductResponse> =
        productRepository.findByNameContainsIgnoreCaseAndDeletedIsFalse(name)
            .map { ProductResponse.Mapper.from(it) }

    @Transactional(readOnly = true)
    @RolesAllowed("admin.products:read")
    fun getById(id: UUID) : ProductResponse =
        productRepository
            .findByIdOrThrow(id)
            .let { ProductResponse.Mapper.from(it) }

    @Transactional
    @RolesAllowed("admin.products:write")
    fun createProduct(request: ProductCreateRequest) : ProductResponse =
        productRepository.save(createEntity(request))
            .let { ProductResponse.Mapper.from(it) }

    @Transactional
    @RolesAllowed("admin.products:write")
    fun updateProduct(id: UUID, request: ProductCreateRequest) : ProductResponse {
//        validate(request)
        val product = productRepository.findByIdOrThrow(id)
            .apply {
                name = request.name
                description = request.description
                price = request.price
                duration = Duration.ofMinutes(request.durationMinutes)
            }
        return productRepository.save(product)
            .let { ProductResponse.Mapper.from(it)}
    }

    @Transactional
    @RolesAllowed("admin.products:write")
    fun deleteProduct(id: UUID) =
        productRepository.findByIdOrThrow(id)
            .apply { deleted = true }

    private fun createEntity(request: ProductCreateRequest) =
        Product(
            request.name,
            request.description,
            request.price,
            Duration.ofMinutes(request.durationMinutes)
        )

//    private fun validate(request: ProductUpdateRequest) {
//        request.name?.let { if (it.isBlank()) throw ValidationException("Имя не должно быть пустым") }
//        request.price?.let { if (it < 0) throw ValidationException("Цена не может быть отрицательной") }
//        request.durationMinutes?.let { if (it <= 0) throw ValidationException("Длительность не может быть нулем или отрицтельной") }
//    }

}