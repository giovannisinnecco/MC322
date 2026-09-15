import java.util.ArrayList;

// Coordena todos os recursos e etapas da fábrica.
public class GerenciadorProducao {

    // Guarda demandas, armazém, máquinas e recursos do turno.
    private final ArrayList<Demanda> demandas = new ArrayList<>();
    private final ArrayList<Produto> produtosFabricados = new ArrayList<>();
    private final ArrayList<Maquina> maquinas = new ArrayList<>();
    private final MateriaPrima materiaPrima;
    private final Esteira esteira = new Esteira();
    private double budget;
    private long tempoTotalProducao;

    // Cria o gerenciador com a linha padrão da fábrica.
    public GerenciadorProducao(MateriaPrima materiaPrima, double budget) {
        this(materiaPrima, budget, new MaquinaFotolitografia(),
                new MaquinaEncapsulamento(), new EstacaoInspecao());
    }

    // Monta a linha com máquinas recebidas na ordem correta.
    public GerenciadorProducao(MateriaPrima materiaPrima, double budget,
                              MaquinaFotolitografia fotolitografia,
                              MaquinaEncapsulamento encapsulamento,
                              EstacaoInspecao inspecao) {
        if (materiaPrima == null || fotolitografia == null
                || encapsulamento == null || inspecao == null) {
            throw new IllegalArgumentException("O estoque e as três máquinas devem ser informados.");
        }
        if (!Double.isFinite(budget) || budget < 0.0) {
            throw new IllegalArgumentException("O orçamento deve ser finito e não negativo.");
        }
        this.materiaPrima = materiaPrima;
        this.budget = budget;
        maquinas.add(fotolitografia);
        maquinas.add(encapsulamento);
        maquinas.add(inspecao);
    }

    // Cadastra uma demanda nova para um modelo conhecido.
    public void registrarDemanda(String tipoProduto, int quantidadeProdutos) {
        obterConsumoPorUnidade(tipoProduto);
        for (Demanda demanda : demandas) {
            if (demanda.getTipoProduto().equals(tipoProduto)) {
                throw new IllegalArgumentException("Já existe demanda para " + tipoProduto + ".");
            }
        }
        demandas.add(new Demanda(tipoProduto, quantidadeProdutos));
    }

    // Substitui a quantidade pendente de uma demanda.
    public void atualizarDemanda(String tipoProduto, int novaQuantidade) {
        buscarDemanda(tipoProduto).atualizarQuantidade(novaQuantidade);
    }

    // Produz até atender o pedido ou acabar algum recurso.
    public void fabricarDemanda(String tipoProduto) {
        Demanda demanda = buscarDemanda(tipoProduto);
        if (demanda.estaAtendida()) {
            System.out.println("[AVISO] Não há unidades pendentes de " + tipoProduto + ".");
            return;
        }

        int consumoPorUnidade = obterConsumoPorUnidade(tipoProduto);
        double custoCiclo = calcularCustoProducao();
        long tentativas = 0;
        int aprovados = 0;
        long tempoInicial = tempoTotalProducao;

        // Liga toda a linha antes de iniciar as tentativas.
        esteira.ligar();
        for (Maquina maquina : maquinas) {
            maquina.ligar();
        }
        try {
            // Repete o ciclo enquanto ainda faltarem unidades aprovadas.
            while (!demanda.estaAtendida()) {
                if (!materiaPrima.verificarDisponibilidade(consumoPorUnidade)) {
                    System.out.println("[AVISO] Produção interrompida: matéria-prima insuficiente.");
                    break;
                }
                if (budget < custoCiclo) {
                    System.out.println("[AVISO] Produção interrompida: orçamento insuficiente para o ciclo completo.");
                    break;
                }

                Produto produto = criarProduto(tipoProduto);
                materiaPrima.consumir(consumoPorUnidade);
                tentativas++;
                // Faz a unidade passar por cada máquina da linha.
                for (Maquina maquina : maquinas) {
                    esteira.adicionarItem(produto);
                    Produto transportado = esteira.removerItem();
                    maquina.processar(transportado);
                    budget -= maquina.getCustoOperacao();
                }
                tempoTotalProducao += produto.calcularTempoProducao();

                // Só uma unidade aprovada atende a demanda.
                if ("Aprovado".equals(produto.getStatus())) {
                    produtosFabricados.add(produto);
                    demanda.atender();
                    aprovados++;
                }
                System.out.println("Produto #" + produto.getId() + " — "
                        + produto.getTipo() + ": " + produto.getStatus());
            }
        } finally {
            // Sempre desliga a linha ao encerrar a execução.
            for (Maquina maquina : maquinas) {
                maquina.desligar();
            }
            esteira.desligar();
        }

        System.out.println("Tentativas: " + tentativas + " | Aprovados: " + aprovados
                + " | Rejeitados: " + (tentativas - aprovados)
                + " | Pendentes: " + demanda.getQuantidadeProdutos());
        System.out.println("Tempo desta execução: " + (tempoTotalProducao - tempoInicial)
                + " unidades de tempo.");
        if (demanda.estaAtendida()) {
            System.out.println("[OK] Demanda de " + tipoProduto + " atendida.");
        }
    }

    // Compra material sem ultrapassar o orçamento disponível.
    public void comprarMateriaPrima(double quantidade) {
        if (!Double.isFinite(quantidade) || quantidade <= 0.0) {
            throw new IllegalArgumentException("A quantidade de compra deve ser finita e positiva.");
        }
        double custoCompra = quantidade * materiaPrima.getCustoPorUnidade();
        if (!Double.isFinite(custoCompra) || custoCompra <= 0.0) {
            throw new IllegalArgumentException("O custo calculado da compra é inválido.");
        }
        if (custoCompra > budget) {
            throw new IllegalStateException("Orçamento insuficiente para a compra.");
        }
        materiaPrima.adicionarEstoque(quantidade);
        budget -= custoCompra;
    }

    // Mostra o dinheiro que ainda pode ser usado.
    public void exibirBudget() {
        System.out.printf("Orçamento disponível: R$ %.2f%n", budget);
    }

    // Lista todas as unidades aprovadas no armazém.
    public void exibirArmazem() {
        System.out.println("=== ARMAZÉM ===");
        if (produtosFabricados.isEmpty()) {
            System.out.println("Nenhum produto armazenado.");
            return;
        }
        for (Produto produto : produtosFabricados) {
            System.out.println("#" + produto.getId() + " — " + produto.getTipo()
                    + " — " + produto.getStatus());
            System.out.println("  Circuito: " + produto.getConfiguracaoCircuito());
        }
        System.out.println("Total armazenado: " + produtosFabricados.size());
    }

    // Mostra a quantidade e o preço do silício.
    public void exibirEstoque() {
        System.out.println(materiaPrima.getNome() + ": " + materiaPrima.getQuantidade()
                + " " + materiaPrima.getUnidade());
        System.out.printf("Preço de compra por unidade: R$ %.2f%n", materiaPrima.getCustoPorUnidade());
    }

    // Mostra as pendências e o material mínimo de cada pedido.
    public void exibirDemandas() {
        for (Demanda demanda : demandas) {
            String tipo = demanda.getTipoProduto();
            System.out.println(tipo + ": " + demanda.getQuantidadeProdutos()
                    + " pendentes | Material mínimo, sem rejeições: "
                    + demanda.calcularMateriaPrimaNecessaria(obterConsumoPorUnidade(tipo)));
        }
    }

    // Libera os totais usados pelo menu e pelos testes.
    public double getBudget() { return budget; }
    public long getTempoTotalProducao() { return tempoTotalProducao; }
    public int getTotalArmazenado() { return produtosFabricados.size(); }

    // Consulta a pendência de um modelo específico.
    public int getQuantidadePendente(String tipoProduto) {
        return buscarDemanda(tipoProduto).getQuantidadeProdutos();
    }

    // Conta no armazém as unidades de um modelo.
    public int getQuantidadeArmazenada(String tipoProduto) {
        obterConsumoPorUnidade(tipoProduto);
        int quantidade = 0;
        for (Produto produto : produtosFabricados) {
            if (produto.getTipo().equals(tipoProduto)) {
                quantidade++;
            }
        }
        return quantidade;
    }

    // Soma o custo de um ciclo completo da linha.
    private double calcularCustoProducao() {
        double custo = 0.0;
        for (Maquina maquina : maquinas) {
            custo += maquina.getCustoOperacao();
        }
        return custo;
    }

    // Localiza a demanda cadastrada para o modelo.
    private Demanda buscarDemanda(String tipoProduto) {
        for (Demanda demanda : demandas) {
            if (demanda.getTipoProduto().equals(tipoProduto)) {
                return demanda;
            }
        }
        throw new IllegalArgumentException("Demanda não registrada para esse tipo de produto.");
    }

    // Retorna o consumo sem criar uma unidade de produto.
    private int obterConsumoPorUnidade(String tipoProduto) {
        if (tipoProduto == null) {
            throw new IllegalArgumentException("O tipo do produto deve ser informado.");
        }
        switch (tipoProduto) {
            case "STM32G": return STM32G.CONSUMO_MATERIA_PRIMA;
            case "STM32F": return STM32F.CONSUMO_MATERIA_PRIMA;
            case "STM32H": return STM32H.CONSUMO_MATERIA_PRIMA;
            default: throw new IllegalArgumentException("Tipo de produto desconhecido: " + tipoProduto);
        }
    }

    // Cria o objeto correto para o modelo solicitado.
    private Produto criarProduto(String tipoProduto) {
        switch (tipoProduto) {
            case "STM32G": return new STM32G();
            case "STM32F": return new STM32F();
            case "STM32H": return new STM32H();
            default: throw new IllegalArgumentException("Tipo de produto desconhecido: " + tipoProduto);
        }
    }
}
