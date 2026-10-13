# Silicon Fab

Simulação de uma fábrica de microcontroladores STM32, desenvolvida por Giovanni
Innecco (RA 282589) e Rodrigo Soares (RA 197361) para MC322.

A linha transforma silício em chips STM32G, STM32F e STM32H, passando por
fotolitografia, encapsulamento e inspeção. Os valores são fictícios.

## Preparação

Instale um **JDK 25** e deixe o Java disponível no `PATH` ou em `JAVA_HOME`.
O projeto inclui o **Gradle Wrapper 9.1.0**; não precisa instalar Gradle à parte.
A primeira execução precisa de internet para baixar a distribuição oficial,
verificada pelo SHA-256 registrado no Wrapper. A aplicação e os testes não usam
bibliotecas externas.

## Compilar, executar e documentar

Na raiz do projeto, pelo PowerShell:

```powershell
.\gradlew.bat build
.\gradlew.bat run --console=plain
.\gradlew.bat javadoc
```

No Linux ou macOS:

```sh
./gradlew build
./gradlew run --console=plain
./gradlew javadoc
```

O `run` recebe a entrada do terminal. Escolha o cenário e use os menus numéricos.
O Javadoc fica em `build/docs/javadoc/index.html`. O `build` compila, executa as
verificações e gera o JAR em `build/libs/` e as distribuições em
`build/distributions/`. Para conferir desde a compilação inicial, use
`./gradlew clean build javadoc` (ou `.\gradlew.bat clean build javadoc`).

## Organização

- `src/main/java/`: classes, interfaces, enums e exceções da fábrica.
- `src/test/java/`: verificações de comportamento e recuperação após erros.
- `build.gradle` e `settings.gradle`: configuração da aplicação Java.
- `gradle/wrapper/`, `gradlew` e `gradlew.bat`: Gradle Wrapper.
- `justificativa.txt`: escolhas da dupla e regras da simulação.

Os arquivos de `build/`, `.gradle/` e o antigo `bin/` são locais e não são
necessários para construir o projeto. No VS Code, abra a raiz e importe o projeto
Gradle; os fontes agora ficam em `src/main/java/`.

## Operação e recuperação

O cenário Ideal oferece mais recursos e menos risco; o Apocalíptico oferece menos
recursos e maior desgaste. As estratégias continuam sendo fila de silício, maior
lote STM32 e maior lote inteiro viável. O armazém guarda apenas chips aprovados.

Erros de digitação pedem uma nova entrada. Erros da fábrica são apresentados no
menu principal, onde é possível escolher outra operação:

- `DemandaInvalidaException`: pedido inexistente, repetido ou em estado inadequado.
- `RecursoInsuficienteException`: falta de orçamento ou silício.
- `EquipamentoIndisponivelException`: máquina quebrada, equipamento parado ou
  limite de capacidade/ocupação excedido.
- `FalhaProducaoException`: erro inesperado em uma etapa, com a causa original
  preservada para diagnóstico.

A unidade de confirmação é uma **tentativa de fabricar um chip**. Falta de
recursos é detectada antes de debitar essa tentativa e cancela o saldo do pedido,
como na tarefa 3. Os chips de tentativas anteriores continuam válidos. Rejeição
na inspeção é um resultado normal: consome recursos e fica registrada na auditoria.

Se uma etapa lançar uma exceção, o ciclo incompleto devolve estoque e orçamento,
restaura saúde e contador de inspeções, esvazia a esteira e não entra no armazém
nem na contagem de tentativas confirmadas. A linha desliga e o pedido volta a
pendente. IDs de objetos criados e sorteios já feitos não são reutilizados.

## Verificações

`build` e `test` executam a tarefa `verificarFabrica`, que chama `TesteFabrica`
diretamente, sem JUnit. Para executar somente essas verificações:

```powershell
.\gradlew.bat verificarFabrica
```

Os casos cobrem estratégias, transições de pedidos, recursos insuficientes,
compras inválidas, capacidades, estados das máquinas, rastreabilidade, cenários,
falha provocada após a inspeção e continuidade do menu. Uma condição incorreta
lança `AssertionError` e faz o build falhar. Os casos de produção usam sementes
fixas para que os resultados sejam reproduzíveis.
