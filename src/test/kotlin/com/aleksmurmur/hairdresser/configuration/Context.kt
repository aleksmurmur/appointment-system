package com.aleksmurmur.hairdresser.configuration


import com.aleksmurmur.hairdresser.booking.repository.BookingRepository
import com.aleksmurmur.hairdresser.client.repository.ClientRepository
import com.aleksmurmur.hairdresser.configuration.postgres.PostgresInitializer
import com.aleksmurmur.hairdresser.product.repository.ProductRepository
import com.aleksmurmur.hairdresser.schedule.repository.ScheduleRepository
import com.aleksmurmur.hairdresser.schedule.repository.TimeslotRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) //FIXME check if needed
@ContextConfiguration(initializers = [PostgresInitializer::class])
@EnableAutoConfiguration //FIXME check if needed (works without)

 class Context {


@Autowired
protected lateinit var testClient: MockMvc

    @Autowired
    protected lateinit var mapper: ObjectMapper

    @Autowired
    protected lateinit var scheduleRepository: ScheduleRepository

    @Autowired
    protected lateinit var clientRepository: ClientRepository

    @Autowired
    protected lateinit var bookingRepository: BookingRepository

    @Autowired
    protected lateinit var productRepository: ProductRepository

    @Autowired
    protected lateinit var timeslotRepository: TimeslotRepository

    fun cleanDB() {
        timeslotRepository.deleteAll()
        scheduleRepository.deleteAll()
        clientRepository.deleteAll()
        bookingRepository.deleteAll()
        productRepository.deleteAll()

    }



}