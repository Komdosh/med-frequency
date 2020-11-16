package com.alunahealth.medfrequency.noteevents

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface NoteEventsProcessedRepository: JpaRepository<NoteEventsProcessed, Long>{

    @Query("SELECT c FROM NoteEventsProcessed c")
    fun find(): NoteEventsProcessed
}
