package com.alunahealth.medfrequency

import com.alunahealth.medfrequency.frequency.MetaMapFrequencyService
import com.alunahealth.medfrequency.noteevents.NoteEventsProcessed
import com.alunahealth.medfrequency.noteevents.NoteEventsProcessedRepository
import com.alunahealth.medfrequency.noteevents.NoteEventsReaderService
import mu.KotlinLogging
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile

const val NOTE_EVENTS_SIZE_2020 = 2_083_180
val log = KotlinLogging.logger {}

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
            noteEventsReaderService.getNoteEvents()
                .forEach { metaMapFrequencyService.buildFrequencies(it) }

        }
    }
}

fun main(args: Array<String>) {
    runApplication<MedFrequencyApplication>(*args)
}
