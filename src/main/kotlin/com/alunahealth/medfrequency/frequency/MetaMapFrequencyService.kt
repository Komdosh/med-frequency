package com.alunahealth.medfrequency.frequency

import com.alunahealth.medfrequency.NOTE_EVENTS_SIZE_2020
import com.alunahealth.medfrequency.log
import com.alunahealth.medfrequency.noteevents.NoteEventsProcessed
import com.alunahealth.medfrequency.noteevents.NoteEventsProcessedRepository
import gov.nih.nlm.nls.metamap.MetaMapApiImpl
import gov.nih.nlm.nls.metamap.Result
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.task.TaskExecutor
import org.springframework.stereotype.Service
import java.text.Normalizer
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Collectors
import java.util.stream.Collectors.groupingBy

@Service
class MetaMapFrequencyService(
    private val frequencyRepository: FrequencyRepository,
    private val noteEventsProcessedRepository: NoteEventsProcessedRepository,
    private val taskExecutor: TaskExecutor,
    @Value("\${app.metamap.ports}") val ports: List<Int> = listOf(8086)
) {

    private val startTime = System.nanoTime()
    private var avgProcessingTime = 0L
    private var processed = 0L

    var tCount = AtomicInteger(0)

    val semaphore = Semaphore(ports.size)

    fun buildFrequencies(input: String) {
        runBlocking {
            semaphore.acquire()

            val text =
                Normalizer.normalize(input, Normalizer.Form.NFD)
                    .replace("[^\\p{ASCII}]".toRegex(), "")

            CoroutineScope(taskExecutor.asCoroutineDispatcher()).launch {
                val processedCitations = processText(text)
                semaphore.release()
                processCitations(processedCitations, text.length)
            }
        }
    }

    private fun processCitations(
        processedCitations: MutableList<Result>,
        textLength: Int
    ) {
        if (processedCitations.isEmpty())
            return
        val concepts = getConcepts(processedCitations)

        log.info("Text size: ${textLength}, concepts: ${concepts.values.size}")
        saveFrequencies(concepts)

        logTime()
    }

    private fun processText(text: String): MutableList<Result> {
        val startTime = System.nanoTime()
        val node = tCount.getAndIncrement() % ports.size
        val api = MetaMapApiImpl()
        api.setPort(ports[node])
        api.options =
            "-i --exclude_sts qnco,tmco,qlco,mnob,popg,idcn,spco,grup,cnce,phpr --exclude_sources NCI_FDA,NLMSubSyn,CST,NCI_CDISC,NCI_NCI-GLOSS,NCI_NICHD,NCI_BRIDG_3_0_3,NCI_CPTAC,NCI_CTCAE,NCI_CTCAE_5"

        log.info("Start text processing ${text.length} on $node node")
        return try {
            val processed = api.processCitationsFromString(text)
            log.info(
                "MetaMap processing {} took {} millis",
                text.length,
                TimeUnit.MILLISECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS)
            )
            processed
        } catch (e: Exception) {
            log.info(e.message)
            mutableListOf()
        } finally {
            api.disconnect()
        }
    }

    private fun saveFrequencies(concepts: Map<Concept, Long>) {
        val frequencies = concepts.entries.map { entry ->

            val freq = frequencyRepository.findByCui(entry.key.conceptId)
                ?: MedTermFrequencies(
                    null,
                    entry.key.conceptId,
                    entry.key.preferredName
                )
            freq.count += entry.value
            return@map freq
        }
        frequencyRepository.saveAll(frequencies)
    }

    private fun logTime() {
        val p = increaseProcessed()

        processed++
        val avg = (System.nanoTime() - startTime) / processed
        avgProcessingTime = avg * (NOTE_EVENTS_SIZE_2020 - processed)

        val (hours, minutes, seconds) = estimatedTime()

        val avgMillis = TimeUnit.MILLISECONDS.convert(avg, TimeUnit.NANOSECONDS)
        log.info("Finished ${p.count} / $NOTE_EVENTS_SIZE_2020 (${((p.count.toDouble() / NOTE_EVENTS_SIZE_2020) * 100).toInt()}%) Estimated Time: $hours:$minutes:$seconds, millis/doc: $avgMillis")
    }

    private fun increaseProcessed(): NoteEventsProcessed {
        val p = noteEventsProcessedRepository.find()

        p.count++

        noteEventsProcessedRepository.save(p)
        return p
    }

    private fun estimatedTime(): Triple<Long, Long, Long> {
        val hours = TimeUnit.HOURS.convert(avgProcessingTime, TimeUnit.NANOSECONDS)
        val minutes = TimeUnit.MINUTES.convert(
            avgProcessingTime - TimeUnit.NANOSECONDS.convert(
                hours,
                TimeUnit.HOURS
            ), TimeUnit.NANOSECONDS
        )
        val seconds = TimeUnit.SECONDS.convert(
            avgProcessingTime - TimeUnit.NANOSECONDS.convert(
                hours,
                TimeUnit.HOURS
            ) - TimeUnit.NANOSECONDS.convert(
                minutes,
                TimeUnit.MINUTES
            ), TimeUnit.NANOSECONDS
        )
        return Triple(hours, minutes, seconds)
    }
}

data class Concept(val conceptId: String, val preferredName: String, val semanticType: String) {
    override fun hashCode(): Int {
        return conceptId.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Concept

        if (conceptId != other.conceptId) return false

        return true
    }

}

fun getConcepts(processedCitations: List<Result>): Map<Concept, Long> {
    val startTime = System.nanoTime()
    val conceptMap = processedCitations
        .stream()
        .unordered()
        .flatMap { it.utteranceList.stream() }
        .flatMap { it.pcmList.stream() }
        .flatMap { it.mappingList.stream() }
        .flatMap { it.evList.stream() }
        .map {
            Concept(
                conceptId = it.conceptId,
                preferredName = it.preferredName,
                it.semanticTypes.toString()
            )
        }
        .collect(
            groupingBy(
                { it },
                Collectors.counting()
            )
        )

    log.info(
        "{} concepts were fetched for {} millis",
        conceptMap.size,
        TimeUnit.MILLISECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS)
    )
    return conceptMap
}
