# Silicon Fab — Tarefa 2 de MC322

Simulação didática de uma fábrica de microcontroladores STM32, desenvolvida por Giovanni Innecco e Rodrigo Soares para a disciplina de Programação Orientada a Objetos.

Integrantes: Giovanni Innecco (RA 282589) e Rodrigo Soares (RA 197361).

## Executar

É necessário um JDK com `javac` e `java` disponíveis no terminal. O código utiliza recursos de Java 8 ou superior.

Na raiz do projeto, execute no PowerShell:

```powershell
javac -encoding UTF-8 -d bin src/*.java
if ($LASTEXITCODE -eq 0) {
    java -cp bin Main
}
```

Em Bash:

```bash
javac -encoding UTF-8 -d bin src/*.java && java -cp bin Main
```

## Usar a fábrica

| Opção | Ação |
|---|---|
| 1–3 | Definir a quantidade pendente de STM32G, STM32F ou STM32H. |
| 4–6 | Fabricar o modelo correspondente. |
| 7 | Consultar o armazém. |
| 8 | Consultar o estoque de silício. |
| 9 | Comprar matéria-prima. |
| 10 | Consultar o resumo do turno e demandas. |
| 0 | Encerrar. |

Para experimentar, escolha `1`, informe `5`, escolha `4` para fabricar e `7` para consultar o armazém. A fábrica tenta obter cinco unidades aprovadas enquanto houver recursos; rejeições geram tentativas adicionais.

Atualizar uma demanda substitui sua quantidade pendente. Zero encerra a pendência, sem remover produtos armazenados. A compra aceita ponto ou vírgula decimal, sem separador de milhar.

## Regras da simulação

- Cada objeto de produto representa um microcontrolador.
- A linha contém fotolitografia, encapsulamento e inspeção.
- O turno começa com R$ 1.000,00 e 100 unidades de material.
- Material comprado custa R$ 2,00 por unidade; cada ciclo completo custa R$ 18,00 em operação.
- Rejeições consomem material, dinheiro e tempo. O armazém recebe apenas produtos aprovados na avaliação final.
- A produção para antes de iniciar uma unidade sem material ou orçamento suficiente.
- O tempo é simulado, sem espera real. Os dados não são salvos entre execuções.

Consumos, perfis de circuito, tempos, custos e níveis de qualidade são fictícios para o exercício. A justificativa explica as diferenças entre os requisitos da tarefa e as escolhas do projeto.

## Organização e verificação

- `src/`: classes da fábrica, gerenciador e menu.
- `justificativa.txt`: motivação do tema e explicação da implementação atual.
- `DECISOES_TAREFA_2.md`: decisões aprovadas e histórico de implementação; as atualizações vigentes estão indicadas no início.
- `tests/`: testes de inspeção, gerenciador e menu. Consulte `tests/README.md` para os comandos.

O código foi compilado com `-Xlint:all` sem avisos Java. A revisão mais recente passou em 300 verificações de inspeção, 49 do gerenciador e 76 do menu.
