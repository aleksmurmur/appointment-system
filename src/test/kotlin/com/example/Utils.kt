package com.example

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.test.web.servlet.ResultActionsDsl
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.ThreadLocalRandom
import kotlin.random.Random

fun randomDuration() : Duration =
    Duration.ofMinutes(Random.nextLong(30, 180))

fun randomFutureDate() : LocalDate
{
    val randomDay = ThreadLocalRandom.current().nextLong(LocalDate.now().toEpochDay(), Int.MAX_VALUE.toLong())
    return LocalDate.ofEpochDay(randomDay)
}

fun randomLocalTime(from: Long = 0) : LocalTime
{
    val randomSec = Random.nextLong(from, LocalTime.MAX.toSecondOfDay().toLong())
    return LocalTime.ofSecondOfDay(randomSec)
}

fun randomPhone() = (1..10)
            .map { ('0'..'9').random() }
            .joinToString("")

    fun randomString(length: Int = 10) : String {
        val chars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }

inline fun <reified T> ResultActionsDsl.response(mapper: ObjectMapper): T =
    mapper.readValue(andReturn().response.contentAsString, T::class.java)

fun <T> ResultActionsDsl.response(mapper: ObjectMapper, reference: TypeReference<T>): T =
    mapper.readValue(andReturn().response.contentAsString, reference)

