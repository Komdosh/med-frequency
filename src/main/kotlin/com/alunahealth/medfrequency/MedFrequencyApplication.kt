package com.alunahealth.medfrequency

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MedFrequencyApplication : CommandLineRunner {
    override fun run(vararg args: String) {
        runExampleText()
    }
}

fun main(args: Array<String>) {
    runApplication<MedFrequencyApplication>(*args)
}
