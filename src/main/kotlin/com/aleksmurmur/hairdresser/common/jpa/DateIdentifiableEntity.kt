package com.aleksmurmur.hairdresser.common.jpa

import jakarta.persistence.*

import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate

@MappedSuperclass
open class DateIdentifiableEntity(id: LocalDate) : JpaPersistable<LocalDate>()
{


    @Id
    @Column( unique = true, nullable = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, pattern = "yyyy-MM-dd")
    override var id: LocalDate? = id


}