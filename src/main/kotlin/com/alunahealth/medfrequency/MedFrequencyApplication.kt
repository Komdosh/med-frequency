package com.alunahealth.medfrequency

import com.alunahealth.medfrequency.frequency.MetaMapFrequencyService
import com.alunahealth.medfrequency.noteevents.NoteEventsProcessed
import com.alunahealth.medfrequency.noteevents.NoteEventsProcessedRepository
import com.alunahealth.medfrequency.noteevents.NoteEventsReaderService
import com.alunahealth.medfrequency.noteevents.NoteEventsReaderService.Companion.COMBINED_TEXTS
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.EnableScheduling


const val TEXTS_LESS_THAN_500_CHARS = 110_166
const val ALL_TEXTS = 491_103
const val NOTE_EVENTS_SIZE_2020 = ALL_TEXTS - TEXTS_LESS_THAN_500_CHARS / COMBINED_TEXTS
val log = KotlinLogging.logger {}


@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties
class MedFrequencyApplication(
    private val metaMapFrequencyService: MetaMapFrequencyService,
    private val noteEventsReaderService: NoteEventsReaderService,
    private val noteEventsProcessedRepository: NoteEventsProcessedRepository
) {

    @Bean
    @Profile("example")
    fun example() = ApplicationRunner {
        runExample()
    }

    @Bean
    @Profile("!example")
    fun run(): ApplicationRunner {

        return ApplicationRunner {
            if (noteEventsProcessedRepository.count() == 0L) {
                noteEventsProcessedRepository.save(NoteEventsProcessed(count = 0))
            }

            log.info("Total documents to process: $NOTE_EVENTS_SIZE_2020")
            noteEventsReaderService.getNoteEvents()
                .forEach {
                    runBlocking {
                        while (resetMutex.isLocked) {
                            log.info("ResetMutex is locked")
                            delay(5000)
                        }
                        metaMapFrequencyService.buildFrequencies(it)
                    }
                }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<MedFrequencyApplication>(*args)
}
