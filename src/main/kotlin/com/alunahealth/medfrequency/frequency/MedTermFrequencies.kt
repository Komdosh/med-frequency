package com.alunahealth.medfrequency.frequency

import javax.persistence.*

@Entity
@Table(indexes = [Index(name="cui", columnList =  "cui", unique = true)])
data class MedTermFrequencies(
    @Id @GeneratedValue val id: Long?,
    val cui: String,
    val prefTerm: String,
    var count: Long = 0
)
