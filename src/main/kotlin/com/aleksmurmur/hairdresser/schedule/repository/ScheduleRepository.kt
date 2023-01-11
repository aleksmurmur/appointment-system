package com.aleksmurmur.hairdresser.schedule.repository

import com.aleksmurmur.hairdresser.schedule.domain.DaySchedule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface ScheduleRepository : JpaRepository<DaySchedule, LocalDate> {


    fun findByIdGreaterThanEqualAndIdLessThanEqual(dateFrom: LocalDate, dateTo: LocalDate) : List<DaySchedule>
}