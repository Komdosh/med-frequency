package com.alunahealth.medfrequency

import com.alunahealth.medfrequency.frequency.MetaMapFrequencyService
import com.alunahealth.medfrequency.noteevents.NoteEventsProcessed
import com.alunahealth.medfrequency.noteevents.NoteEventsProcessedRepository
import com.alunahealth.medfrequency.noteevents.NoteEventsReaderService
import mu.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

val log = KotlinLogging.logger {}

@SpringBootApplication
class MedFrequencyApplication(
    private val metaMapFrequencyService: MetaMapFrequencyService,
    private val noteEventsReaderService: NoteEventsReaderService,
    private val noteEventsProcessedRepository: NoteEventsProcessedRepository
) : CommandLineRunner {

    override fun run(vararg args: String) {
        //runExampleText()

        if (noteEventsProcessedRepository.count() == 0L) {
            noteEventsProcessedRepository.save(NoteEventsProcessed(count = 0))
        }

        noteEventsReaderService.getNoteEvents()
            .stream()
            .map { metaMapFrequencyService.buildFrequencies(it) }
            .forEach {

                val p = noteEventsProcessedRepository.find()
                log.info("finished ${p.count}/")
                p.count++
                noteEventsProcessedRepository.save(p)
            }

    }
}

fun main(args: Array<String>) {
    runApplication<MedFrequencyApplication>(*args)
}
