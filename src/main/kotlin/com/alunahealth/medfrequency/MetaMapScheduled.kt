package com.alunahealth.medfrequency

import com.alunahealth.medfrequency.frequency.MetaMapFrequencyService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

val resetMutex: Mutex = Mutex()

@Service
class MetaMapScheduled(
    @Value("\${app.metamap.script.start}")
    private val startScript: String,
    @Value("\${app.metamap.script.stop}")
    private val stopScript: String,
    private val taskExecutor: TaskExecutor,
    private val metaMapFrequencyService: MetaMapFrequencyService,
) {


    @Scheduled(fixedDelay = 4 * 60 * 60 * 1000) //run every 4 hours
    @ConditionalOnProperty(
        "scheduled",
        prefix = "app.metamap.script",
        havingValue = "true",
        matchIfMissing = false
    )
    fun scheduled() {
        CoroutineScope(taskExecutor.asCoroutineDispatcher()).launch {

            if (resetMutex.isLocked) {
                return@launch
            }
            log.info("Run metamap restart")

            resetMutex.lock()
            while (metaMapFrequencyService.semaphore.availablePermits != metaMapFrequencyService.ports.size) {
                log.info(
                    "semaphore {}/{}",
                    metaMapFrequencyService.semaphore.availablePermits,
                    metaMapFrequencyService.ports.size
                )
                delay(5000)
            }

            log.info("Run metamap stop script")
            val stopProcess: Process = Runtime.getRuntime().exec(String.format(stopScript))

            val stopExitCode = stopProcess.waitFor()
            log.info("Stop process executed with {} code", stopExitCode)

            log.info("Run metamap start script")
            val startProcess: Process = Runtime.getRuntime().exec(String.format(startScript))

            val startExitCode = startProcess.waitFor()
            log.info("Start process executed with {} code", startExitCode)

            log.info("Run gc")
            Runtime.getRuntime().gc()
            resetMutex.unlock()
        }
    }
}
