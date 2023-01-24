package com.aleksmurmur.hairdresser.web.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping


@Controller
class LoginController {

    @RequestMapping("/login")
    fun login(): String {
        return "login"
    }

    @RequestMapping("/", "")
    fun welcome(): String {
        return "index"
    }

    @RequestMapping("/login-error")
    fun loginError(model: Model): String? {
        model.addAttribute("loginError", true)
        return "login"
    }


}