import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

// Inicia a fábrica e cuida da conversa com o usuário.
public class Main {

    // Mantém a ordem dos modelos usada pelo menu.
    private static final String[] TIPOS_PRODUTO = {"STM32G", "STM32F", "STM32H"};

    // Monta o turno e mantém o sistema aberto até a saída.
    public static void main(String[] args) {
        Locale.setDefault(Locale.forLanguageTag("pt-BR"));
        MateriaPrima silicio = new MateriaPrima(1, "Silício", 100.0,
                "unidades de material", 10.0, 2.0);
        GerenciadorProducao gerenciador = new GerenciadorProducao(silicio, 1000.0);
        for (String tipo : TIPOS_PRODUTO) {
            gerenciador.registrarDemanda(tipo, 0);
        }

        exibirIntroducao();
        try (Scanner scanner = new Scanner(System.in)) {
            executarMenu(scanner, gerenciador);
        } catch (NoSuchElementException erro) {
            System.out.println("\n[SISTEMA] Entrada encerrada. Finalizando o turno.");
        }
        exibirResumo(gerenciador);
        System.out.println("[SISTEMA] Turno encerrado. Até logo!");
    }

    // Apresenta o tema e as regras básicas da simulação.
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

    // Recebe as escolhas e encaminha cada ação ao gerenciador.
    private static void executarMenu(Scanner scanner, GerenciadorProducao gerenciador) {
        while (true) {
            exibirMenu(gerenciador);
            int opcao = lerInteiro(scanner, "Escolha uma opção (0-10): ", 0, 10);
            if (opcao == 0) {
                return;
            }
            try {
                if (opcao >= 1 && opcao <= 3) {
                    String tipo = TIPOS_PRODUTO[opcao - 1];
                    System.out.println("Atualizar " + tipo + ": substitui a pendência atual.");
                    int quantidade = lerInteiro(scanner,
                            "Informe a nova quantidade pendente (0 para encerrar a pendência): ",
                            0, Integer.MAX_VALUE);
                    gerenciador.atualizarDemanda(tipo, quantidade);
                    System.out.println("[OK] Demanda atualizada: " + quantidade + " unidades pendentes.");
                } else if (opcao >= 4 && opcao <= 6) {
                    gerenciador.fabricarDemanda(TIPOS_PRODUTO[opcao - 4]);
                } else {
                    switch (opcao) {
                        case 7:
                            gerenciador.exibirArmazem();
                            break;
                        case 8:
                            gerenciador.exibirEstoque();
                            break;
                        case 9:
                            gerenciador.exibirEstoque();
                            double quantidade = lerQuantidadeCompra(scanner);
                            gerenciador.comprarMateriaPrima(quantidade);
                            System.out.println("[OK] Compra realizada: " + quantidade + " unidades de material.");
                            gerenciador.exibirBudget();
                            break;
                        case 10:
                            exibirResumo(gerenciador);
                            break;
                        default:
                            throw new IllegalArgumentException("Opção inválida.");
                    }
                }
            } catch (IllegalArgumentException | IllegalStateException erro) {
                System.out.println("[ERRO] " + erro.getMessage());
            }
        }
    }

    // Mostra as opções e o estado atual das demandas.
    private static void exibirMenu(GerenciadorProducao gerenciador) {
        System.out.println("\n================ MENU DA SILICON FAB =================");
        gerenciador.exibirBudget();
        System.out.println("\nATUALIZAR DEMANDAS");
        for (int i = 0; i < TIPOS_PRODUTO.length; i++) {
            String tipo = TIPOS_PRODUTO[i];
            System.out.println((i + 1) + " - Atualizar " + tipo
                    + " (pendentes: " + gerenciador.getQuantidadePendente(tipo) + ")");
        }
        System.out.println("\nFABRICAR");
        for (int i = 0; i < TIPOS_PRODUTO.length; i++) {
            System.out.println((i + 4) + " - Fabricar " + TIPOS_PRODUTO[i]);
        }
        System.out.println("\nCONSULTAR");
        System.out.println("7 - Ver armazém");
        System.out.println("8 - Ver estoque de matéria-prima");
        System.out.println("10 - Ver resumo do turno e demandas");
        System.out.println("\nCOMPRAR MATÉRIA-PRIMA");
        System.out.println("9 - Comprar silício");
        System.out.println("\n0 - Sair");
    }

    // Lê um inteiro válido dentro do intervalo pedido.
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
                // A mensagem abaixo orienta uma nova tentativa.
            }
            System.out.println("[ERRO] Digite um número inteiro entre " + minimo + " e " + maximo + ".");
        }
    }

    // Lê uma compra positiva com ponto ou vírgula decimal.
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

    // Resume recursos, demandas e resultados do turno.
    private static void exibirResumo(GerenciadorProducao gerenciador) {
        System.out.println("\n================ RESUMO DO TURNO =====================");
        gerenciador.exibirBudget();
        gerenciador.exibirEstoque();
        gerenciador.exibirDemandas();
        int totalCriado = Produto.getTotalProdutosFabricados();
        int totalArmazenado = gerenciador.getTotalArmazenado();
        System.out.println("Microcontroladores produzidos (inclui rejeitados): " + totalCriado);
        System.out.println("Aprovados no armazém: " + totalArmazenado);
        System.out.println("Rejeitados: " + (totalCriado - totalArmazenado));
        System.out.println("Tempo total de produção: " + gerenciador.getTempoTotalProducao()
                + " unidades de tempo.");
    }
}
