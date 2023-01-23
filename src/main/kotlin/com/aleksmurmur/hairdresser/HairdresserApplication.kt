package com.aleksmurmur.hairdresser

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication
@EnableTransactionManagement
class HairdresserApplication

fun main(args: Array<String>) {
    runApplication<HairdresserApplication>(*args)
}
