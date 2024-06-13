# Teste de Carga

## Validate Transaction

- Execução do teste do `validate-transaction`: `mvn gatling:test -Dgatling.simulationClass=transaction.HeavyLoadValidateTransaction`

### Resultado com JdkClientHttpRequestFactory

| Total | OK | KO | Min Response (ms) | Max Response (ms) | Mean Response (ms) |
|-------|----|----|-------------------|-------------------|--------------------|
|160550|160550| 0  | 1                 | 2014              | 3                  |

### Resultado com SimpleClientHttpRequestFactory

| Total | OK | KO | Min Response (ms) | Max Response (ms) | Mean Response (ms) |
|-------|----|----|-------------------|-------------------|--------------------|
|160550|160550| 0  | 2                 | 1787              | 4                  |

### Resultado com JettyClientHttpRequestFactory

| Total | OK | KO | Min Response (ms) | Max Response (ms) | Mean Response (ms) |
|-------|----|----|-------------------|-------------------|--------------------|
|160550|160550| 0  | 1                 | 2680              | 3                  |