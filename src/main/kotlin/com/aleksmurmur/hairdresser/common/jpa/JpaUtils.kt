package com.aleksmurmur.hairdresser.common.jpa


import com.aleksmurmur.hairdresser.exception.EntityNotFoundException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import java.io.Serializable


inline fun <reified T : JpaPersistable<*>> T?.throwExceptionIfNotFound(paramName: String, param: Any): T {
    return this ?: throw EntityNotFoundException("${T::class.simpleName} was not found by $paramName = $param")
}

inline fun <reified T : JpaPersistable<I>, I : Serializable, R : JpaRepository<T, I>> R.findByIdOrThrow(id: I): T {
    return findByIdOrNull(id).throwExceptionIfNotFound("id", id)
}