package com.alunahealth.medfrequency.frequency

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FrequencyRepository: JpaRepository<MedTermFrequencies, Long>{

    fun findByCui(cui: String): MedTermFrequencies?
}
