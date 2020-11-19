package com.alunahealth.medfrequency.frequency

import com.alunahealth.medfrequency.NOTE_EVENTS_SIZE_2020
import com.alunahealth.medfrequency.log
import com.alunahealth.medfrequency.noteevents.NoteEventsProcessed
import com.alunahealth.medfrequency.noteevents.NoteEventsProcessedRepository
import gov.nih.nlm.nls.metamap.Ev
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
        taskExecutor.execute {
            val concepts = getConcepts(processedCitations)

            log.info("Text size: ${textLength}, concepts: ${concepts.values.size}")
            saveFrequencies(concepts)

            logTime()
        }
    }

    private fun processText(text: String): MutableList<Result> {
        val node = tCount.getAndIncrement() % ports.size
        val api = MetaMapApiImpl()
        api.setPort(ports[node])
        api.options =
            "-i --exclude_sts qnco,tmco,qlco --exclude_sources NCI_FDA,NLMSubSyn,CST,NCI_CDISC,NCI_NCI-GLOSS,NCI_NICHD"

        log.info("Start text processing ${text.length} on $node node")
        return try {
            api.processCitationsFromString(text)
        } catch (e: Exception) {
            log.info(e.message)
            mutableListOf()
        } finally {
            api.disconnect()
        }
    }

    private fun saveFrequencies(concepts: Map<String, List<Ev>>) {
        val frequencies = concepts.values.map { list ->
            val mapEv = list[0]

            val freq = frequencyRepository.findByCui(mapEv.conceptId)
                ?: MedTermFrequencies(
                    null,
                    mapEv.conceptId,
                    mapEv.preferredName
                )
            freq.count += list.size
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

        val avgSec = TimeUnit.SECONDS.convert(avg, TimeUnit.NANOSECONDS)
        log.info("finished ${p.count} / $NOTE_EVENTS_SIZE_2020 (${((p.count.toDouble() / NOTE_EVENTS_SIZE_2020) * 100).toInt()}%) Estimated Time: $hours:$minutes:$seconds, sec/doc: $avgSec")
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

fun getConcepts(processedCitations: List<Result>): Map<String, List<Ev>> {
    return processedCitations
        .parallelStream()
        .unordered()
        .flatMap { it.utteranceList.stream() }
        .flatMap { it.pcmList.stream() }
        .flatMap { it.mappingList.stream() }
        .flatMap { it.evList.stream() }
        .filter {
            it.preferredName.length > 1
        }
        .collect(groupingBy { it.conceptId })
}
