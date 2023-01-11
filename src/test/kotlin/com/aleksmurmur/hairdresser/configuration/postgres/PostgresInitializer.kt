package com.aleksmurmur.hairdresser.configuration.postgres

import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext

class PostgresInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        val postgresDbContainer = PostgresContainer()
            .apply {
                this.withReuse(true)
                start()
            }
        applicationContext.beanFactory.registerSingleton(postgresDbContainer::class.simpleName!!, postgresDbContainer)
    }
}