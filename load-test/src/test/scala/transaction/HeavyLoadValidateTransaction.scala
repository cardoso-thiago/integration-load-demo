package transaction

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration.DurationInt

class HeavyLoadValidateTransaction extends Simulation {

  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl("http://localhost:8080")
    .header("Content-Type", "application/json")

  val scn: ScenarioBuilder =
    scenario("RunTransactions")
      .exec(
        http("createTransaction")
          .post("/transaction/v1")
          .body(StringBody(
            """{
              "value": 100.0,
              "user": {
                "name": "John Doe",
                "document": "123456789",
                "validation": 1
              },
              "transactionStatus": "CREATED"
            }"""
          )).asJson
      )
      .pause(10)

  setUp(
    scn.inject(
      // 50 usuários enviando transações simultaneamente
      atOnceUsers(50),
      // 50 novos usuários a cada segundo
      constantUsersPerSec(50).during(1.minutes),
      // Aumenta de 50 para 100 usuários por segundo, no intervalo de 1 minuto
      rampUsersPerSec(50).to(100).during(1.minutes),
      // 100 novos usuários por segundo
      constantUsersPerSec(100).during(1.minutes),
      // Aumenta de 100 para 200 usuários por segundo, no intervalo de 1 minuto
      rampUsersPerSec(100).to(200).during(1.minutes),
      // 200 novos usuários por segundo
      constantUsersPerSec(200).during(1.minutes),
      // Aumenta de 200 para 400 usuários por segundo, no intervalo de 1 minuto
      rampUsersPerSec(200).to(400).during(1.minutes),
      // 400 novos usuários por segundo
      constantUsersPerSec(400).during(1.minutes),
      // Aumenta de 400 para 800 usuários por segundo, no intervalo de 1 minuto
      rampUsersPerSec(400).to(800).during(1.minutes),
      // 800 novos usuários por segundo
      constantUsersPerSec(800).during(1.minutes),
    ).protocols(httpProtocol)
  )
}