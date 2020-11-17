package com.alunahealth.medfrequency.noteevents

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.util.stream.Stream

@Service
class NoteEventsReaderService(
    @Value("\${app.noteEvents}") private val noteEvents: Resource,
    private val noteEventsProcessedRepository: NoteEventsProcessedRepository
) {

    companion object {
        const val COMBINED_TEXTS = 5
    }

    fun getNoteEvents(): Stream<String> {

        val skip = noteEventsProcessedRepository.find().count

        val csvParser = CSVParser(
            noteEvents.file.bufferedReader(),
            CSVFormat.DEFAULT.withIgnoreHeaderCase()
        )
        val iterator = csvParser.iterator()

        val texts = mutableSetOf<String>()
        return Stream.generate<CSVRecord> { null }
            .takeWhile { iterator.hasNext() }
            .map { iterator.next() }
            .skip(skip * COMBINED_TEXTS)
            .map { it.get(10) }
            .map { texts.add(it) }
            .filter { texts.size >= COMBINED_TEXTS }
            .map {
                texts.reduce { acc, s -> acc + s }
            }
    }
}
