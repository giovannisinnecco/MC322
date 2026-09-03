import java.util.Scanner;

public class Main {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=======================================================");
        System.out.println("           SILICON FAB - MICROCONTROLADORES STM32      ");
        System.out.println("=======================================================");
        System.out.println("Aqui transformamos Silício em microcontroladores");
        System.out.println("da família STM32.");
        System.out.println("Desenvolvido por: Giovanni Innecco e Rodrigo Soares");
        System.out.println("=======================================================\n");

        MateriaPrima silicio = new MateriaPrima(1, "Silício", 1000.0, "gramas", 10.0);

        Produto stm32f4 = new Produto(1, "STM32F401 (Lote)", 5.0);
        stm32f4.definirDemandaMateriaPrima(5.0); 
        
        Produto stm32g0 = new Produto(2, "STM32G071 (Lote)", 3.0);
        stm32g0.definirDemandaMateriaPrima(3.0);
        
        Produto stm32h7 = new Produto(3, "STM32H753 (Lote)", 10.0);
        stm32h7.definirDemandaMateriaPrima(10.0);

        Maquina fotolitografia = new Maquina("Máquina de Fotolitografia EUV", 50.0);
        Esteira esteira = new Esteira(100.0);
        EstacaoInspecao microscopio = new EstacaoInspecao();

        System.out.println("[SISTEMA] Equipamentos inicializados e prontos para operar.\n");


        while (true) {
            System.out.println("\n=======================================================");
            System.out.println("                 MENU PRINCIPAL                        ");
            System.out.println("=======================================================");
            System.out.println("1 - Iniciar produção");
            System.out.println("2 - Consultar estoque de matéria-prima");
            System.out.println("3 - Sair");
            System.out.print("Escolha uma opção: ");

            while (!scanner.hasNextInt()) {
                System.out.println("\n[ERRO] Entrada inválida. Por favor, digite apenas números.");
                System.out.print("Escolha uma opção: ");
                scanner.next();
            }
            int opcao = scanner.nextInt();

            if (opcao == 1) {
                System.out.println("\n[SISTEMA] Acessando módulo de produção...");
                System.out.println("\nProdutos Disponíveis:");
                System.out.println("1 - " + stm32f4.getNome());
                System.out.println("2 - " + stm32g0.getNome());
                System.out.println("3 - " + stm32h7.getNome());
                System.out.print("Selecione o produto (1-3): ");
                while (!scanner.hasNextInt()) {
                    System.out.println("[ERRO] Entrada inválida. Digite um número de 1 a 3.");
                    scanner.next();
                }
                int escolhaProduto = scanner.nextInt();

                Produto produtoSelecionado = null;
                if (escolhaProduto == 1) produtoSelecionado = stm32f4;
                else if (escolhaProduto == 2) produtoSelecionado = stm32g0;
                else if (escolhaProduto == 3) produtoSelecionado = stm32h7;
                else {
                    System.out.println("[ERRO] Opção de produto inválida.");
                    continue;
                }

                System.out.print("Informe a demanda de matéria-prima (gramas): ");
                while (!scanner.hasNextDouble()) {
                    System.out.println("[ERRO] Entrada inválida. Digite um valor numérico para a demanda.");
                    scanner.next();
                }
                double demanda = scanner.nextDouble();

                System.out.println("\n[SISTEMA] Iniciando fluxo de produção...");

                if (!silicio.verificarDisponibilidade(demanda)) {
                    System.out.println("[ERRO] Estoque insuficiente de " + silicio.getNome() + ".");
                    continue; 
                }
                System.out.println("[OK] Demanda aprovada. Material reservado.");

                esteira.ligar();
                fotolitografia.ligar();
                microscopio.ativar();
                System.out.println("[OK] Equipamentos energizados e operantes.");

                esteira.adicionarItem(silicio, demanda);
                Object itemTransportado = esteira.removerItem();
                System.out.println("[OK] Matéria-prima transportada até a " + fotolitografia.getNome() + ".");

                fotolitografia.processar((MateriaPrima) itemTransportado, demanda); 
                produtoSelecionado.processar(); 
                System.out.println("[OK] Processamento concluído. Microcontrolador " + produtoSelecionado.getNome() + " criado.");

                esteira.adicionarItem(produtoSelecionado, 1.0);
                Object produtoFinal = esteira.removerItem();
                System.out.println("[OK] Produto transportado para a área de qualidade.");

                microscopio.inspecionar((Produto) produtoFinal);
                
                esteira.desligar();
                fotolitografia.desligar();
                microscopio.desativar();

                System.out.println("\n=== PRODUÇÃO CONCLUÍDA COM SUCESSO ===");
                
            } else if (opcao == 2) {
                System.out.println("\n=== ESTOQUE ATUAL ===");
                System.out.println("Material: " + silicio.getNome());
                System.out.println("Quantidade disponível: " + silicio.getQuantidade() + " gramas");
                
            } else if (opcao == 3) {
                System.out.println("\n[SISTEMA] Desligando equipamentos e encerrando o turno. Até logo!");
                break;
                
            } else {
                System.out.println("\n[ERRO] Opção inválida. Digite 1, 2 ou 3.");
            }
        }

        scanner.close();

        
    }
}
