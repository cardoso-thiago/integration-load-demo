package br.com.cardoso.controller

import br.com.cardoso.service.TransactionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/transactions")
class TransactionController(private val transactionService: TransactionService) {

    @GetMapping
    fun findAllTransactions() = ResponseEntity.ok(transactionService.findAllTransactions())

    @GetMapping("/{id}")
    fun findRevisionsById(@PathVariable("id") id: Long) = ResponseEntity.ok(transactionService.findRevisionsById(id))

    //Apenas para validação do mecanismo de auditoria
    @DeleteMapping("/{id}")
    fun deleteTransactionById(@PathVariable("id") id: Long): ResponseEntity<Void> {
        transactionService.deleteTransactionById(id)
        return ResponseEntity.noContent().build()
    }
}