package com.example.hairdresser.common.jpa

import dev.codefish.smstrerching.core.exception.EntityNotPersistedException
import java.io.Serializable

abstract class JpaPersistable<T : Serializable> {

    abstract var id: T?
        protected set

    val persistentId: T
        get() = id ?: throw EntityNotPersistedException("The entity hasn't persisted yet.")

}