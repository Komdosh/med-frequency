package com.alunahealth.medfrequency.noteevents

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.util.stream.Collectors.toSet
import java.util.stream.Stream

@Service
class NoteEventsReaderService(
    @Value("\${app.noteEvents}") private val noteEvents: Resource,
    @Value("\${app.subjectIds}") private val subjectIds: Resource,
    private val noteEventsProcessedRepository: NoteEventsProcessedRepository
) {

    companion object {
        const val COMBINED_TEXTS = 1
    }

    fun getNoteEvents(): Stream<String> {
        val subjects =
            if (subjectIds.exists()) subjectIds.file.bufferedReader().lines().collect(toSet())
            else setOf()

        val skip = noteEventsProcessedRepository.find().count

        val csvParser = CSVParser(
            noteEvents.file.bufferedReader(),
            CSVFormat.DEFAULT.withIgnoreHeaderCase()
        )
        val iterator = csvParser.iterator()

        var csvStream = Stream.generate<CSVRecord> { null }
            .takeWhile { iterator.hasNext() }
            .map { iterator.next() }
            .skip(skip * COMBINED_TEXTS)

        if (subjects.isNotEmpty()) {
            csvStream = csvStream
                .filter { subjects.contains(it.get(1)) }
        }

        var textStream = csvStream
            .map { it.get(10) }

        if (COMBINED_TEXTS > 1) {
            val texts = mutableSetOf<String>()
            textStream = textStream
                .map { texts.add(it) }
                .filter { texts.size >= COMBINED_TEXTS }
                .map {
                    val text = texts.reduce { acc, s -> acc + "\n" + s }
                    texts.clear()
                    return@map text
                }
        }

        return textStream
    }
}
