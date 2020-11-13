package com.alunahealth.medfrequency

import mu.KotlinLogging
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.File
import java.util.stream.Stream
import kotlin.streams.toList

val log = KotlinLogging.logger {}

@SpringBootApplication
class MedFrequencyApplication : CommandLineRunner {

    val NOTE_EVENTS_PATH = "C:\\Projects\\mimic\\NOTEEVENTS.csv"

    override fun run(vararg args: String) {
        //runExampleText()

        val metaMapFrequency = MetaMapFrequency()
        val iterator = CSVParser(
            File(NOTE_EVENTS_PATH).bufferedReader(),
            CSVFormat.DEFAULT.withIgnoreHeaderCase()
        ).iterator()

        var index = 0
        Stream.generate<CSVRecord> { null }
            .takeWhile { iterator.hasNext() }
            .map { iterator.next() }
            .skip(1)
            .limit(2)
            .map { it.get(10) }
            .toList()
            .parallelStream()
            .map { metaMapFrequency.buildFrequencies(it) }
            .forEach { log.info("finished ${++index}") }

        println(metaMapFrequency.frequencies)
    }
}

fun main(args: Array<String>) {
    runApplication<MedFrequencyApplication>(*args)
}
