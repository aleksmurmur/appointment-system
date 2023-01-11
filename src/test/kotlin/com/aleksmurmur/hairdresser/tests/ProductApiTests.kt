package com.aleksmurmur.hairdresser.tests

import com.aleksmurmur.hairdresser.api.PRODUCTS_PATH
import com.aleksmurmur.hairdresser.configuration.Context
import com.aleksmurmur.hairdresser.product.domain.Product
import com.aleksmurmur.hairdresser.product.dto.ProductCreateRequest
import com.aleksmurmur.hairdresser.product.dto.ProductResponse
import com.aleksmurmur.hairdresser.product.dto.ProductUpdateRequest
import com.example.product
import com.example.productCreateRequest
import com.example.productUpdateRequest
import com.example.response
import com.fasterxml.jackson.core.type.TypeReference
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import java.util.UUID

class ProductApiTests : Context() {

    lateinit var product1: Product
    lateinit var product2: Product

    @BeforeEach
    fun before() {
        cleanDB()
        productRepository.save(product()).run { product1 = this }
        productRepository.save(product()).run { product2 = this }
    }

    @Nested
    inner class GetById {
        @Test
        fun `successfully gets by id`() {
            getProduct(product1.persistentId)
                .andExpect { status { isOk() } }
                .response<ProductResponse>(mapper)
                .run {
                    assertEquals(product1.persistentId, id)
                    assertEquals(product1.name, name)
                    assertEquals(product1.description, description)
                    assertEquals(product1.price, price)
                    assertEquals(product1.duration.toMinutes(), durationMinutes)
                    assertEquals(product1.deleted, deleted)
                }
        }

        @Test
        fun `returns 404 if non-existent id handled`() {
            getProduct(UUID.randomUUID())
                .andExpect { status { isNotFound() } }
        }

        private fun getProduct(id: UUID) =
            testClient.get("$PRODUCTS_PATH/$id")
    }

    @Nested
    inner class GetAll {
        @Test
        fun `returns all products`() {
            getProducts()
                .andExpect { status { isOk() } }
                .response(mapper, object : TypeReference<List<ProductResponse>>() {})
                .run {
                    assertEquals(2, size)
                    assertEquals(product1.persistentId, first().id)
                }
        }

        private fun getProducts() =
            testClient.get(PRODUCTS_PATH)
    }

    @Nested
    inner class Create {
        @Test
        fun `creates product`() {
            val request = productCreateRequest()
            createProduct(request)
                .andExpect { status { isOk() } }
                .response<ProductResponse>(mapper)
                .run {
                    assertEquals(request.name, name)
                    assertEquals(request.description, description)
                    assertEquals(request.price, price)
                    assertEquals(request.durationMinutes, durationMinutes)
                }
        }

        @Test
        fun `returns 400 if name is empty`() {
            val request = productCreateRequest().copy(name = "")
            createProduct(request)
                .andExpect { status { isBadRequest() } }
        }

        @Test
        fun `returns 400 if price is negative`() {
            val request = productCreateRequest().copy(price = -1)
            createProduct(request)
                .andExpect { status { isBadRequest() } }
        }

        @Test
        fun `returns 400 if duration is zero`() {
            val request = productCreateRequest().copy(durationMinutes = 0)
            createProduct(request)
                .andExpect { status { isBadRequest() } }
        }

        private fun createProduct(request: ProductCreateRequest) =
            testClient.post(PRODUCTS_PATH) {
                content = mapper.writeValueAsString(request)
                contentType = MediaType.APPLICATION_JSON
            }
    }

    @Nested
    inner class Update {
        @Test
        fun `successfully updates product`() {
            val request = productUpdateRequest()
            updateProduct(product1.persistentId, request)
                .andExpect { status { isOk() } }
                .response<ProductResponse>(mapper)
                .run {
                    assertEquals(request.name, name)
                    assertEquals(request.description, description)
                    assertEquals(request.price, price)
                    assertEquals(request.durationMinutes, durationMinutes)
                }
        }

        @Test
        fun `partly updates product`() {
            val request = productUpdateRequest().copy(name = null, durationMinutes = null)
            updateProduct(product1.persistentId, request)
                .andExpect { status { isOk() } }
                .response<ProductResponse>(mapper)
                .run {
                    assertEquals(product1.name, name)
                    assertEquals(request.description, description)
                    assertEquals(request.price, price)
                    assertEquals(product1.duration.toMinutes(), durationMinutes)
                }
        }

        @Test
        fun `returns 404 if non-existent id handled`() {
            updateProduct(UUID.randomUUID(), productUpdateRequest())
                .andExpect { status { isNotFound() } }
        }

        @Test
        fun `returns 400 if name is blank`() {
            updateProduct(product1.persistentId, productUpdateRequest().copy(name = "   "))
                .andExpect { status { isBadRequest() } }
        }

        @Test
        fun `returns 400 if price is negative`() {
            updateProduct(product1.persistentId, productUpdateRequest().copy(price = -1))
                .andExpect { status { isBadRequest() } }
        }

        @Test
        fun `returns 400 if duration is negative`() {
            updateProduct(product1.persistentId, productUpdateRequest().copy(durationMinutes = -1))
                .andExpect { status { isBadRequest() } }
        }

        private fun updateProduct(id: UUID, request: ProductUpdateRequest) =
            testClient.patch("$PRODUCTS_PATH/$id") {
                content = mapper.writeValueAsString(request)
                contentType = MediaType.APPLICATION_JSON
            }
    }

    @Nested
    inner class Delete {
        @Test
        fun `deletes by id`() {
            deleteProduct(product1.persistentId)
                .andExpect { status { isNoContent() } }
        }

        @Test
        fun `returns 404 if non-existent id handled`() {
            deleteProduct(UUID.randomUUID())
                .andExpect { status { isNotFound() } }
        }

        private fun deleteProduct(id: UUID) =
            testClient.delete("$PRODUCTS_PATH/$id")
    }

    }