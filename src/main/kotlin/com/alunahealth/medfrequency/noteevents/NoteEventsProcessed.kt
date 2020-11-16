package com.alunahealth.medfrequency.noteevents

import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class NoteEventsProcessed(@Id val id: Long = 1, var count: Long)
