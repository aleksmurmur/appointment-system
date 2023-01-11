package com.aleksmurmur.hairdresser.client.repository

import com.aleksmurmur.hairdresser.client.domain.Client
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ClientRepository : JpaRepository<Client, UUID> {

    fun findByDeletedIsFalse() : List<Client>

    fun findByPhoneContains(phone: String) : List<Client>
}