package com.example.hairdresser.client.domain

import com.example.hairdresser.common.jpa.UUIDIdentifiableEntity

class Client (
    var phone: String,
    var name: String? = null,
        ): UUIDIdentifiableEntity() {
}