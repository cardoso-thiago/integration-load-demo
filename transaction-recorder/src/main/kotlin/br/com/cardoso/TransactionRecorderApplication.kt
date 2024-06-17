package br.com.cardoso

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TransactionRecorderApplication

fun main(args: Array<String>) {
	runApplication<TransactionRecorderApplication>(*args)
}
