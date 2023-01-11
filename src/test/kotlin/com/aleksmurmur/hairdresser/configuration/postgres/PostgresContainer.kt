package com.aleksmurmur.hairdresser.configuration.postgres

import org.testcontainers.containers.PostgreSQLContainer

class PostgresContainer : PostgreSQLContainer<PostgresContainer>(DOCKER_POSTGRES_NAME) {

    companion object {
        private const val DOCKER_POSTGRES_NAME = "postgres:14.5"
    }

    override fun start() {
        super.start()

        System.setProperty("spring.datasource.url", jdbcUrl)
        System.setProperty("spring.datasource.username", username)
        System.setProperty("spring.datasource.password", password)
    }

    override fun close() {
//        super.close()
    }
}