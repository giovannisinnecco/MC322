import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Apresenta a fábrica e recebe as escolhas numéricas do turno.
 * @author Giovanni Innecco e Rodrigo Soares
 */
public class Main {
    /** A entrada da fábrica é o método main. */
    private Main() { }


    private static final String[] TIPOS_PRODUTO = {"STM32G", "STM32F", "STM32H"};

    /**
     * Abre o turno e encerra normalmente quando a entrada termina.
     * @param args argumentos de execução, não utilizados
     */
    public static void main(String[] args) {
        Locale.setDefault(Locale.forLanguageTag("pt-BR"));
        exibirIntroducao();
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("CENÁRIO: 1 - Ideal | 2 - Apocalíptico | 0 - Sair");
            int escolha = lerInteiro(scanner, "Escolha: ", 0, 2);
            if (escolha == 0) return;
            Cenario cenario = escolha == 1 ? Cenario.IDEAL : Cenario.APOCALIPTICO;
            GerenciadorProducao gerenciador = new GerenciadorProducao(cenario, new EstrategiaFilaSilicio());
            for (String tipo : TIPOS_PRODUTO) gerenciador.registrarDemanda(tipo, 0);
            executarMenu(scanner, gerenciador);
            exibirResumo(gerenciador);
        } catch (NoSuchElementException erro) {
            System.out.println("\n[SISTEMA] Entrada encerrada. Finalizando o turno.");
        }
        System.out.println("[SISTEMA] Turno encerrado. Até logo!");
    }

    /**
     * Apresenta o tema, os modelos e os integrantes da dupla.
     */
    private static void exibirIntroducao() {
        System.out.println("=======================================================");
        System.out.println("          SILICON FAB - MICROCONTROLADORES STM32");
        System.out.println("=======================================================");
        System.out.println("Do silício ao microcontrolador: STM32G, STM32F e STM32H.");
        System.out.println("Fotolitografia -> Encapsulamento -> Inspeção");
        System.out.println("Registre quantas unidades aprovadas deseja produzir.");
        System.out.println("Rejeições geram novas tentativas enquanto houver recursos.");
        System.out.println("O material é comprado pelo menu; cada tentativa tem custo.");
        System.out.println("Consumos, tempos e qualidade são fictícios nesta simulação.");
        System.out.println("Desenvolvido por: Giovanni Innecco e Rodrigo Soares");
        System.out.println("=======================================================");
    }

    /**
     * Recebe operações e apresenta erros recuperáveis antes de voltar ao menu.
     * @param scanner leitor da entrada do terminal
     * @param g gerenciador do turno
     */
    private static void executarMenu(Scanner scanner, GerenciadorProducao g) {
        while (true) {
            System.out.println("\n================ SILICON FAB ================");
            System.out.println("Cenário: " + g.getCenario().getNome());
            System.out.println("Estratégia atual: " + g.getNomeEstrategia());
            g.exibirBudget();
            System.out.println("1 - Demandas\n2 - Fabricação\n3 - Consultas\n4 - Comprar silício"
                    + "\n5 - Trocar estratégia\n6 - Auditoria\n0 - Sair");
            int opcao = lerInteiro(scanner, "Escolha: ", 0, 6);
            if (opcao == 0) return;
            try {
                switch (opcao) {
                    case 1: menuDemandas(scanner, g); break;
                    case 2: menuFabricacao(scanner, g); break;
                    case 3: menuConsultas(scanner, g); break;
                    case 4:
                        g.comprarMateriaPrima(lerQuantidadeCompra(scanner));
                        System.out.println("[OK] Silício recebido no estoque."); break;
                    case 5: menuEstrategias(scanner, g); break;
                    case 6: g.gerarAuditoriaGeral(); break;
                    default: break;
                }
            } catch (PlantaException erro) {
                System.out.println("[FÁBRICA] " + erro.getMessage());
            } catch (IllegalArgumentException erro) {
                System.out.println("[ERRO] " + erro.getMessage());
            }
        }
    }
    /**
     * Permite atualizar e consultar os saldos dos pedidos.
     * @param scanner leitor da entrada do terminal
     * @param g gerenciador do turno
     */
    private static void menuDemandas(Scanner scanner, GerenciadorProducao g) {
        while (true) {
            System.out.println("\n--- DEMANDAS ---\n1 - Atualizar STM32G\n2 - Atualizar STM32F"
                    + "\n3 - Atualizar STM32H\n4 - Listar demandas\n0 - Voltar");
            int opcao = lerInteiro(scanner, "Escolha: ", 0, 4);
            if (opcao == 0) return;
            if (opcao == 4) g.exibirDemandas();
            else {
                int quantidade = lerInteiro(scanner, "Nova pendência (0 encerra; atualização reabre pedido cancelado): ",
                        0, Integer.MAX_VALUE);
                g.atualizarDemanda(TIPOS_PRODUTO[opcao - 1], quantidade);
            }
        }
    }
    /**
     * Oferece produção manual ou pela estratégia ativa.
     * @param scanner leitor da entrada do terminal
     * @param g gerenciador do turno
     */
    private static void menuFabricacao(Scanner scanner, GerenciadorProducao g) {
        while (true) {
            System.out.println("\n--- FABRICAÇÃO ---\nEstratégia: " + g.getNomeEstrategia());
            System.out.println("1 - Próxima demanda pela estratégia\n2 - Fabricar STM32G"
                    + "\n3 - Fabricar STM32F\n4 - Fabricar STM32H\n0 - Voltar");
            int opcao = lerInteiro(scanner, "Escolha: ", 0, 4);
            if (opcao == 0) return;
            if (opcao == 1) g.executarProximaProducao();
            else g.fabricarDemanda(TIPOS_PRODUTO[opcao - 2]);
        }
    }
    /**
     * Separa consultas de chips acabados, silício e resumo.
     * @param scanner leitor da entrada do terminal
     * @param g gerenciador do turno
     */
    private static void menuConsultas(Scanner scanner, GerenciadorProducao g) {
        while (true) {
            System.out.println("\n--- CONSULTAS ---\n1 - Armazém de chips acabados"
                    + "\n2 - Estoque de silício bruto\n3 - Resumo do turno\n0 - Voltar");
            int opcao = lerInteiro(scanner, "Escolha: ", 0, 3);
            if (opcao == 0) return;
            if (opcao == 1) g.exibirArmazem();
            if (opcao == 2) g.exibirEstoque();
            if (opcao == 3) exibirResumo(g);
        }
    }
    /**
     * Troca a regra de seleção sem recriar a fábrica.
     * @param scanner leitor da entrada do terminal
     * @param g gerenciador do turno
     */
    private static void menuEstrategias(Scanner scanner, GerenciadorProducao g) {
        System.out.println("\n--- ESTRATÉGIAS ---\n1 - Fila de silício (ordem de chegada)"
                + "\n2 - Maior lote STM32\n3 - Rendimento de chips (maior lote inteiro viável)\n0 - Voltar");
        int opcao = lerInteiro(scanner, "Escolha: ", 0, 3);
        if (opcao == 1) g.setEstrategia(new EstrategiaFilaSilicio());
        if (opcao == 2) g.setEstrategia(new EstrategiaMaiorLoteSTM32());
        if (opcao == 3) g.setEstrategia(new EstrategiaRendimentoChips());
    }

    /**
     * Repete a leitura até receber um inteiro dentro dos limites.
     * @param scanner leitor da entrada do terminal
     * @param mensagem texto apresentado ao usuário
     * @param minimo menor valor aceito
     * @param maximo maior valor aceito
     * @return inteiro válido dentro do intervalo
     * @throws java.util.NoSuchElementException se a entrada do terminal terminar
     */
    private static int lerInteiro(Scanner scanner, String mensagem, int minimo, int maximo) {
        while (true) {
            System.out.print(mensagem);
            String entrada = scanner.nextLine().trim();
            try {
                int valor = Integer.parseInt(entrada);
                if (valor >= minimo && valor <= maximo) {
                    return valor;
                }
            } catch (NumberFormatException erro) {
                // Pede outro número sem encerrar o programa.
            }
            System.out.println("[ERRO] Digite um número inteiro entre " + minimo + " e " + maximo + ".");
        }
    }

    /**
     * Aceita quantidade positiva com ponto ou vírgula decimal, sem milhar.
     * @param scanner leitor da entrada do terminal
     * @return quantidade finita e positiva
     * @throws java.util.NoSuchElementException se a entrada do terminal terminar
     */
    private static double lerQuantidadeCompra(Scanner scanner) {
        while (true) {
            System.out.print("Quantidade de material a comprar (ex.: 5 ou 2,5; sem separador de milhar): ");
            String entrada = scanner.nextLine().trim();
            if (entrada.matches("[0-9]+([.,][0-9]+)?")) {
                double quantidade = Double.parseDouble(entrada.replace(',', '.'));
                if (Double.isFinite(quantidade) && quantidade > 0.0) {
                    return quantidade;
                }
            }
            System.out.println("[ERRO] Digite uma quantidade numérica, finita e maior que zero.");
        }
    }

    /**
     * Mostra os recursos e resultados confirmados no turno.
     * @param gerenciador gerenciador do turno
     */
    private static void exibirResumo(GerenciadorProducao gerenciador) {
        System.out.println("\n================ RESUMO DO TURNO =====================");
        gerenciador.exibirBudget();
        gerenciador.exibirEstoque();
        gerenciador.exibirDemandas();
        int totalCriado = gerenciador.getTotalTentativas();
        int totalArmazenado = gerenciador.getTotalArmazenado();
        System.out.println("Microcontroladores produzidos (inclui rejeitados): " + totalCriado);
        System.out.println("Aprovados no armazém: " + totalArmazenado);
        System.out.println("Rejeitados: " + (totalCriado - totalArmazenado));
        System.out.println("Tempo total de produção: " + gerenciador.getTempoTotalProducao()
                + " unidades de tempo.");
    }
}
