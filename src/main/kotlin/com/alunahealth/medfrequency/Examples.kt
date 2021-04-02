package com.alunahealth.medfrequency

import gov.nih.nlm.nls.metamap.MetaMapApi
import gov.nih.nlm.nls.metamap.MetaMapApiImpl
import gov.nih.nlm.nls.metamap.Result
import gov.nih.nlm.nls.metamap.Utils
import java.text.Normalizer

fun runExample(){
    var input = "Systemic arterial hypertension (hereafter referred to as hypertension) " +
            "is characterized by persistently high blood pressure (BP) in the systemic arteries." +
            " BP is commonly expressed as the ratio of the systolic BP (that is, the pressure " +
            "that the blood exerts on the arterial walls when the heart contracts) and the diastolic" +
            " BP (the pressure when the heart relaxes). The BP thresholds that define hypertension" +
            " depend on the measurement method (Table 1). Several aetiologies can underlie hypertension." +
            " The majority (90–95%) of patients have a highly heterogeneous essential or primary" +
            " hypertension with a multifactorial gene-environment aetiology. A positive family history" +
            " is a frequent occurrence in patients with hypertension, with the heritability" +
            " (a measure of how much of the variation in a trait is due to variation in genetic factors)" +
            " estimated between 35% and 50% in the majority of studies. Genome-wide association studies " +
            "(GWAS) have identified ~120 loci that are associated with BP regulation and together explain " +
            "3.5% of the trait variance. These findings are becoming increasingly important as we search " +
            "for new pathways and new biomarkers to develop more-modern omics-driven diagnostic and " +
            "therapeutic modalities for hypertension in the era of precision medicine."
    input = Normalizer.normalize(input, Normalizer.Form.NFD)
    input = input.replace("[^\\p{ASCII}]".toRegex(), "")
    val api: MetaMapApi = MetaMapApiImpl()

    println("api instantiated")

    val resultList = api.processCitationsFromString(input)
    resultList.forEach { result: Result ->
        try {
            val negations = result.negationList
            for (negation in negations) {
                println("conceptposition:" + negation.conceptPositionList)
                println("concept pairs:" + negation.conceptPairList)
                println("trigger positions: " + negation.triggerPositionList)
                println("trigger: " + negation.trigger)
                println("type: " + negation.type)
            }
            for (utterance in result.utteranceList) {
                println("Utterance:")
                println("  Id: " + utterance.id)
                println("  Utterance text: " + utterance.string)
                println("  Position: " + utterance.position)
                for (pcm in utterance.pcmList) {
                    println("Mappings:")
                    for (map in pcm.mappingList) {
                        println("Phrase:")
                        println("  Map Score: " + map.score)
                        for (mapEv in map.evList) {
                            println("  Score: " + mapEv.score)
                            println("  Concept Id: " + mapEv.conceptId)
                            println("  Concept Name: " + mapEv.conceptName)
                            println("  Preferred Name: " + mapEv.preferredName)
                            println("  Matched Words: " + mapEv.matchedWords)
                            println("  Semantic Types: " + mapEv.semanticTypes)
                            println("  MatchMap: " + mapEv.matchMap)
                            println("  MatchMap alt. repr.: " + mapEv.matchMapList)
                            println("  is Head?: " + mapEv.isHead)
                            println("  is Overmatch?: " + mapEv.isOvermatch)
                            println("  Sources: " + mapEv.sources)
                            println("  Positional Info: " + mapEv.positionalInfo)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
