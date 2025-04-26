package com.example.kotlin.chat

import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator

@Configuration
class ReactiveDbConfig {

    /**
     * Ensures that the table’s schema is initialized when the application starts up.
     */
    @Suppress("unused") //used by spring
    @Bean
    fun initializer(connectionFactory: ConnectionFactory) : ConnectionFactoryInitializer {

        val databasePopulator = CompositeDatabasePopulator()
        databasePopulator.addPopulators(ResourceDatabasePopulator(ClassPathResource("./sql/schema.sql")))

        val connectionFactoryInitializer = ConnectionFactoryInitializer()
        with(connectionFactoryInitializer) {
            setConnectionFactory(connectionFactory)
            setDatabasePopulator (databasePopulator)

        }

        return connectionFactoryInitializer

    }
}