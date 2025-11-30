package com.agendly

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AgendlyApplication

fun main(args: Array<String>) {
  runApplication<AgendlyApplication>(*args)
}
