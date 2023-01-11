package com.aleksmurmur.hairdresser.product.api

import com.aleksmurmur.hairdresser.api.PRODUCTS_PATH
import com.aleksmurmur.hairdresser.product.dto.ProductCreateRequest
import com.aleksmurmur.hairdresser.product.dto.ProductResponse
import com.aleksmurmur.hairdresser.product.dto.ProductUpdateRequest
import com.aleksmurmur.hairdresser.product.service.ProductService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping(PRODUCTS_PATH)
class ProductController (
    private val productService: ProductService
        ){

    @GetMapping
    fun getAll() : ResponseEntity<List<ProductResponse>> =
        ResponseEntity.ok(productService.getAll())

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID) : ResponseEntity<ProductResponse> =
        ResponseEntity.ok(productService.getById(id))

    @PostMapping
    fun createProduct(@RequestBody @Valid request: ProductCreateRequest) : ResponseEntity<ProductResponse> =
        ResponseEntity.ok(productService.createProduct(request))

    @PatchMapping("/{id}")
    fun updateProduct(@PathVariable id: UUID, @RequestBody @Valid request: ProductUpdateRequest) : ResponseEntity<ProductResponse> =
        ResponseEntity.ok(productService.updateProduct(id, request))

    @DeleteMapping("/{id}")
    fun deleteProduct(@PathVariable id: UUID) : ResponseEntity<Unit> =
        productService.deleteProduct(id)
            .let { ResponseEntity.noContent().build() }

}