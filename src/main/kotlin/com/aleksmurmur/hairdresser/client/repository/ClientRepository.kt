package com.example.hairdresser.client.repository

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

class ClientRepository : JpaRepository<Client, UUID> {
}