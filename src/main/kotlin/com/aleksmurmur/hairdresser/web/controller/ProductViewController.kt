package com.aleksmurmur.hairdresser.web.controller

import com.aleksmurmur.hairdresser.api.PRODUCTS_VIEW_PATH
import com.aleksmurmur.hairdresser.product.service.ProductService
import com.aleksmurmur.hairdresser.web.dto.ProductCreateOrUpdateForm
import com.aleksmurmur.hairdresser.web.dto.ProductCreateOrUpdateForm.Mapper.toCreateRequest
import com.aleksmurmur.hairdresser.web.dto.ProductCreateOrUpdateForm.Mapper.toForm
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.support.SessionStatus
import java.util.*

@Controller
@RequestMapping(PRODUCTS_VIEW_PATH)
class ProductViewController(
    private val productService: ProductService
) {



    @GetMapping("/new")
    fun initCreationForm(
                        model: Model, request: HttpServletRequest): String {
        model.addAttribute("productForm", ProductCreateOrUpdateForm())
        model.addAttribute("path", request.servletPath)
        model.addAttribute("action", "create")
        return "products/createOrUpdateProductForm"
    }

    @PostMapping("/new")
    fun processCreationForm(@[Valid ModelAttribute("productForm")] form: ProductCreateOrUpdateForm, result: BindingResult, session: SessionStatus, model: Model): String {
        return if (result.hasErrors()) "products/createOrUpdateProductForm"
        else {
            val response = productService.createProduct(form.toCreateRequest())
            session.setComplete()
            "redirect:${PRODUCTS_VIEW_PATH}/${response.id}"
        }
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID, model: Model) : String {
        model.addAttribute(productService.getById(id))
        return "products/productResponse"
    }

    @GetMapping("")
    fun getAll(@RequestParam name: String?, model: Model, request: HttpServletRequest): String {
        val products = if (name == null) productService.getAll() else productService.getByNameLike(name)
    model.addAttribute("products", products)
        model.addAttribute("path", request.servletPath)
    return "products/productsList"
    }

    @GetMapping("/{id}/edit")
    fun initUpdateForm(@PathVariable id: UUID, model: Model, request: HttpServletRequest): String {
        model.addAttribute("productForm", productService.getById(id).toForm())
        model.addAttribute("path", request.servletPath)
        model.addAttribute("action", "update")
        return "products/createOrUpdateProductForm"
    }

    @PostMapping("/{id}/edit")
    fun processUpdateForm(@PathVariable id: UUID, @[Valid ModelAttribute("productForm")] form: ProductCreateOrUpdateForm, result: BindingResult, session: SessionStatus, model: Model) : String {
        return if (result.hasErrors()) "products/createOrUpdateProductForm"
        else {
            productService.updateProduct(id, form.toCreateRequest())
            session.setComplete()
            "redirect:${PRODUCTS_VIEW_PATH}/{id}"
        }
    }

    @PostMapping("/{id}/delete")
    fun deleteProduct(@PathVariable id: UUID): String {
        productService.deleteProduct(id)
        return "redirect:${PRODUCTS_VIEW_PATH}"
    }




}