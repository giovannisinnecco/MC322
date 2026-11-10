# Silicon Fab

Simulação de uma fábrica de microcontroladores STM32, desenvolvida por Giovanni
Innecco (RA 282589) e Rodrigo Soares (RA 197361) para MC322.

A linha transforma silício em chips STM32G, STM32F e STM32H, passando por
fotolitografia, encapsulamento e inspeção. Os valores são fictícios.

## Escopo e Limitações

### Dados persistidos e não persistidos

O arquivo `dados/planta.xml` registra cenário, orçamento, estratégia, estoque de
silício (identificação, quantidade, unidade, mínimo e custo), máquinas (tipo,
capacidade, saúde, estado, custo, risco-base e limiar de manutenção), os componentes que formam
cada máquina (controlador, sensor e atuador) e demandas com saldo e estado. Esses
campos bastam para reconstruir o estado operacional e continuar a produção.

O armazém, histórico de tentativas e auditorias, sementes e estados internos dos
geradores aleatórios, contador de inspeções, lotes e tempo acumulado permanecem
em memória. São dados históricos ou transitórios e não são necessários para
atender os saldos atuais; persistir esses dados também exigiria reconstruir chips
e suas relações de rastreabilidade. A carga cria uma fábrica separada e só a
substitui no menu depois de validar o documento inteiro. XML ausente, corrompido
ou incompatível gera `PersistenciaException`. A gravação usa arquivo temporário
e substituição atômica quando suportada.

### Suíte de testes automatizados

`PersistenciaPlantaTest` usa JUnit 5 e arquivos em diretório temporário para
verificar persistência/recuperação de estoque, máquinas, estratégia e estados de
demanda, além de arquivos ausentes e corrompidos. Também executa `VerificadorFabrica`,
que cobre estratégias, regras do domínio, produção, recursos insuficientes,
capacidades, falhas e continuidade do menu. O Gradle gera o relatório em
`build/reports/tests/test/index.html`.

## Preparação

Instale um **JDK 25** e deixe o Java disponível no `PATH` ou em `JAVA_HOME`.
O projeto inclui o **Gradle Wrapper 9.1.0**; não precisa instalar Gradle à parte.
A primeira execução precisa de internet para baixar a distribuição oficial,
verificada pelo SHA-256 registrado no Wrapper, e as dependências JUnit do Maven
Central.

## Compilar, executar e documentar

Na raiz do projeto, pelo PowerShell, configure o terminal para UTF-8 antes de executar:

```powershell
chcp 65001
$OutputEncoding = [Console]::OutputEncoding = [System.Text.UTF8Encoding]::new()
.\gradlew.bat build
.\gradlew.bat run --console=plain
.\gradlew.bat test
.\gradlew.bat javadoc
```

No Linux ou macOS:

```sh
./gradlew build
./gradlew run --console=plain
./gradlew test
./gradlew javadoc
```

O `run` recebe a entrada do terminal. Escolha o cenário e use os menus numéricos.
O estado inicial vem de `dados/planta.xml` quando disponível; sem arquivo, inicia
com os recursos do cenário escolhido. As opções 7 e 8 salvam e carregam o estado.
O ajuste da página de código precisa ser feito no mesmo terminal que inicia o Gradle;
por isso o PowerShell acima configura UTF-8 antes do `run`.
O Javadoc fica em `build/docs/javadoc/index.html`. O `build` compila, executa as
verificações e gera o JAR em `build/libs/` e as distribuições em
`build/distributions/`. Para conferir desde a compilação inicial, use
`./gradlew clean build javadoc` (ou `.\gradlew.bat clean build javadoc`).

## Organização

- `src/main/java/br/unicamp/ic/mc322/`: classe principal da aplicação.
- `src/main/java/br/unicamp/ic/mc322/model/`: entidades e componentes de domínio.
- `src/main/java/br/unicamp/ic/mc322/service/`: gerenciador e fluxo de produção.
- `src/main/java/br/unicamp/ic/mc322/strategy/`: estratégias de seleção.
- `src/main/java/br/unicamp/ic/mc322/exception/`: exceções do domínio e persistência.
- `src/main/java/br/unicamp/ic/mc322/persistence/`: leitura e escrita XML.
- `src/test/java/br/unicamp/ic/mc322/`: testes JUnit e verificações legadas.
- `build.gradle` e `settings.gradle`: configuração da aplicação Java.
- `gradle/wrapper/`, `gradlew` e `gradlew.bat`: Gradle Wrapper.
- `justificativa.txt`: escolhas da dupla e regras da simulação.
- `docs/uml/tarefa5-silicon-fab.png` e `.puml`: diagrama UML da composição.

Os arquivos de `build/`, `.gradle/` e o antigo `bin/` são locais e não são
necessários para construir o projeto. No VS Code, abra a raiz e importe o projeto
Gradle; os fontes ficam na estrutura de pacotes sob `src/main/java/`.

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

`build` e `test` executam os testes JUnit 5 e a tarefa `verificarFabrica`, que
mantém as verificações anteriores da fábrica. Para executar as verificações
legadas diretamente:

```powershell
.\gradlew.bat verificarFabrica
```

Os testes de persistência usam arquivos temporários isolados e não leem ou gravam
em `dados/`.

## Composição da Tarefa 5

`GerenciadorProducao` delega a auditoria estrutural e o acesso às etapas à
`SalaLimpa`, que agrupa as máquinas da linha. Cada `Maquina` cria internamente
seu `ControladorEtapa`, `SensorAlinhamento` e `AtuadorPrecisao`; a validação de
ciclo passa por essas partes e a auditoria é apresentada de forma hierárquica.
O diagrama de classes está em `docs/uml/` (PNG para consulta e PlantUML editável).

`PersistenciaPlanta` concentra o formato XML, a validação e a reconstrução dos
objetos, sem delegar operações de arquivo às entidades de domínio. O arquivo local
em `dados/` não precisa ser incluído na release; a aplicação inicializa a fábrica
com valores padrão quando ele ainda não existe.
