package com.alunahealth.medfrequency.noteevents

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.util.stream.Stream
import kotlin.streams.toList

@Service
class NoteEventsReaderService(
    @Value("\${app.noteevents}") private val noteEvents: Resource,
    private val noteEventsProcessedRepository: NoteEventsProcessedRepository
) {
    fun getNoteEvents(limit: Long = 10): List<String> {

        val skip = noteEventsProcessedRepository.find().count

        val csvParser = CSVParser(
            noteEvents.file.bufferedReader(),
            CSVFormat.DEFAULT.withIgnoreHeaderCase()
        )
        val iterator = csvParser.iterator()

        return Stream.generate<CSVRecord> { null }
            .takeWhile { iterator.hasNext() }
            .map { iterator.next() }
            .skip(skip)
            .limit(limit)
            .map { it.get(10) }
            .toList()
    }
}
