package com.aleksmurmur.hairdresser.common.jpa

import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.GenericGenerator
import java.util.*

@MappedSuperclass
open class UUIDIdentifiableEntity : JpaPersistable<UUID>() {

    @Id
    @GenericGenerator(name = "uuid-gen", strategy = "uuid2")
    @GeneratedValue(generator = "uuid-gen")
    override var id: UUID? = null

}