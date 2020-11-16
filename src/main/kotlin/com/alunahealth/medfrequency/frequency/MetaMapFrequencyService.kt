package com.alunahealth.medfrequency.frequency

import gov.nih.nlm.nls.metamap.MetaMapApi
import gov.nih.nlm.nls.metamap.MetaMapApiImpl
import org.springframework.stereotype.Service
import java.text.Normalizer

@Service
class MetaMapFrequencyService(private val frequencyRepository: FrequencyRepository) {

    fun buildFrequencies(input: String, port: Int = 8066) {
        val text =
            Normalizer.normalize(input, Normalizer.Form.NFD).replace("[^\\p{ASCII}]".toRegex(), "")
        val api: MetaMapApi = MetaMapApiImpl()
        api.session.port = port

        api.processCitationsFromString(text).forEach { result ->
            for (utterance in result.utteranceList) {
                for (pcm in utterance.pcmList) {
                    for (map in pcm.mappingList) {
                        for (mapEv in map.evList) {
                            if(mapEv.preferredName.length<2){
                                continue
                            }
                            val freq = frequencyRepository.findByCui(mapEv.conceptId)
                                    ?: MedTermFrequencies(
                                        null,
                                        mapEv.conceptId,
                                        mapEv.preferredName
                                    )

                            ++freq.count
                            frequencyRepository.save(freq)
                        }
                    }
                }
            }
        }

    }
}
