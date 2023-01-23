package com.aleksmurmur.hairdresser.quartz

import com.aleksmurmur.hairdresser.booking.domain.BookingStatus
import com.aleksmurmur.hairdresser.booking.repository.BookingRepository
import mu.KLogging
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class BookingsStatusUpdateJob (
    private val bookingRepository: BookingRepository
        ): Job {

    companion object: KLogging()

    @Transactional
    override fun execute(context: JobExecutionContext) {
    val searched = bookingRepository.findBookingsToComplete()
        .map {
            it.bookingStatus = BookingStatus.COMPLETED
            return}
        bookingRepository.saveAll(searched)
        if (searched.isNotEmpty()) logger.debug { """
            ${searched.size} bookings marked as completed
        """ } else { logger.debug { "Nothing to mark completed" }}

    }
}