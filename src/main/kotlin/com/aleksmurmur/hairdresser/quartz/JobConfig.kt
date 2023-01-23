package com.aleksmurmur.hairdresser.quartz

import org.quartz.JobBuilder
import org.quartz.JobDetail
import org.quartz.SimpleScheduleBuilder
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JobConfig {

    @Bean("BookingsCompletionJobDetail")
 fun jobDetail() : JobDetail {
     return JobBuilder.newJob().
     ofType(BookingsStatusUpdateJob::class.java)
         .storeDurably()
         .withIdentity("BookingsCompletionJob")
         .build()
 }

    @Bean("BookingsCompletionJobTrigger")
    fun trigger(@Qualifier("BookingsCompletionJobDetail")  job : JobDetail) : Trigger {
        return TriggerBuilder.newTrigger().forJob(job)
            .withIdentity("MarkCompletedTrigger")
            .withSchedule(SimpleScheduleBuilder
                .repeatMinutelyForever(10))
            .build()
    }
}