package com.alunahealth.medfrequency

import gov.nih.nlm.nls.metamap.MetaMapApi
import gov.nih.nlm.nls.metamap.MetaMapApiImpl
import java.text.Normalizer

class MetaMapFrequency {

    data class MedTermFrequencies(val cui: String, val prefTerm: String, var count: Long = 0)

    val frequencies = HashMap<String, MedTermFrequencies>()

    fun buildFrequencies(input: String) {
        val text =
            Normalizer.normalize(input, Normalizer.Form.NFD).replace("[^\\p{ASCII}]".toRegex(), "")
        val api: MetaMapApi = MetaMapApiImpl()

        api.processCitationsFromString(text).forEach { result ->
            for (utterance in result.utteranceList) {
                for (pcm in utterance.pcmList) {
                    for (map in pcm.mappingList) {
                        for (mapEv in map.evList) {
                            val freq = frequencies.computeIfAbsent(
                                mapEv.conceptId
                            ) { MedTermFrequencies(mapEv.conceptId, mapEv.preferredName) }
                            ++freq.count
                        }
                    }
                }
            }
        }
    }
}
