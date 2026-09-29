import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Cuida dos pedidos e dos recursos da fábrica.
public class GerenciadorProducao {

    private final ArrayList<Demanda> demandas = new ArrayList<>();
    private final ArrayList<Produto> produtosFabricados = new ArrayList<>();
    private final ArrayList<Maquina> maquinas = new ArrayList<>();
    private final MateriaPrima materiaPrima;
    private final Esteira esteira = new Esteira();
    private double budget;
    private EstrategiaProducao estrategiaAtual;
    private Cenario cenario;
    private int ultimoLote;
    private final List<Produto> produtosTentados = new ArrayList<>();
    private long tempoTotalProducao;

    public GerenciadorProducao(MateriaPrima materiaPrima, double budget) {
        this(materiaPrima, budget, new MaquinaFotolitografia(),
                new MaquinaEncapsulamento(), new EstacaoInspecao());
    }

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

    public void registrarDemanda(String tipoProduto, int quantidadeProdutos) {
        obterConsumoPorUnidade(tipoProduto);
        for (Demanda demanda : demandas) {
            if (demanda.getTipoProduto().equals(tipoProduto)) {
                throw new IllegalArgumentException("Já existe demanda para " + tipoProduto + ".");
            }
        }
        demandas.add(new Demanda(tipoProduto, quantidadeProdutos,
                obterConsumoPorUnidade(tipoProduto), calcularCustoProducao()));
    }

    public void atualizarDemanda(String tipoProduto, int novaQuantidade) {
        Demanda demanda = buscarDemanda(tipoProduto);
        boolean reabrindo = demanda.getStatus() == StatusDemanda.CONCLUIDA
                || demanda.getStatus() == StatusDemanda.CANCELADA;
        demanda.atualizarQuantidade(novaQuantidade);
        // Só pedidos reabertos entram no fim da fila.
        if (reabrindo && novaQuantidade > 0) {
            demandas.remove(demanda);
            demandas.add(demanda);
        }
    }

    public void fabricarDemanda(String tipoProduto) {
        Demanda demanda = buscarDemanda(tipoProduto);
        if (demanda.getStatus() != StatusDemanda.PENDENTE) {
            System.out.println("[AVISO] Demanda não elegível; atualize o pedido de " + tipoProduto + ".");
            return;
        }

        for (Maquina maquina : maquinas) {
            if (maquina.getEstado() == EstadoMaquina.QUEBRADA) {
                System.out.println("[AVISO] Linha parada: " + maquina.getNome() + " quebrada.");
                return;
            }
        }
        int consumoPorUnidade = obterConsumoPorUnidade(tipoProduto);
        if (!esteira.verificarCarga(consumoPorUnidade)) {
            System.out.println("[AVISO] Carga excede a capacidade da esteira; pedido permanece pendente.");
            return;
        }
        for (Maquina maquina : maquinas) {
            if (!maquina.verificarCapacidade(consumoPorUnidade)) {
                System.out.println("[AVISO] Carga excede a capacidade de " + maquina.getNome()
                        + "; pedido permanece pendente.");
                return;
            }
        }
        demanda.iniciar();
        int lote = ++ultimoLote;
        double custoCiclo = calcularCustoProducao();
        long tentativas = 0;
        int aprovados = 0;
        long tempoInicial = tempoTotalProducao;

        try {
            esteira.ligar();
            for (Maquina maquina : maquinas) {
                maquina.ligar();
            }
            while (demanda.getStatus() == StatusDemanda.EM_PRODUCAO) {
                if (!materiaPrima.verificarDisponibilidade(consumoPorUnidade)) {
                    demanda.cancelar();
                    System.out.println("[AVISO] Demanda cancelada: matéria-prima insuficiente ou estoque abaixo do mínimo.");
                    break;
                }
                if (budget < custoCiclo) {
                    demanda.cancelar();
                    System.out.println("[AVISO] Demanda cancelada: orçamento insuficiente para o ciclo completo.");
                    break;
                }

                boolean quebrada = false;
                for (Maquina maquina : maquinas) quebrada |= maquina.getEstado() == EstadoMaquina.QUEBRADA;
                if (quebrada) {
                    demanda.pausar();
                    System.out.println("[AVISO] Linha parada por desgaste. Demanda volta a pendente.");
                    break;
                }
                Produto produto = criarProduto(tipoProduto);
                produto.setLote(lote);
                if (cenario != null) produto.aumentarProbabilidadeFalha(cenario.getRiscoProduto());
                produtosTentados.add(produto);
                PorcaoMateriaPrima porcao = materiaPrima.retirarPorcao(consumoPorUnidade);
                esteira.adicionarMateriaPrima(porcao);
                produto.registrarOrigem(esteira.removerMateriaPrima());
                System.out.println("[ESTEIRA] " + porcao.descrever() + " transportado para o chip #" + produto.getId());
                tentativas++;
                for (Maquina maquina : maquinas) {
                    esteira.adicionarItem(produto);
                    Produto transportado = esteira.removerItem();
                    maquina.processar(transportado);
                    budget -= maquina.getCustoOperacao();
                }
                tempoTotalProducao += produto.calcularTempoProducao();

                // Só chips aprovados reduzem a pendência.
                if (produto.getStatus() == StatusProduto.APROVADO) {
                    produtosFabricados.add(produto);
                    demanda.atender();
                    aprovados++;
                }
                System.out.println("Produto #" + produto.getId() + " — "
                        + produto.getTipo() + ": " + produto.getStatus());
            }
        } finally {
            // Mesmo com erro, a linha desliga e o pedido volta à fila.
            if (demanda.getStatus() == StatusDemanda.EM_PRODUCAO) demanda.pausar();
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

    public void exibirBudget() {
        System.out.printf("Orçamento disponível: R$ %.2f%n", budget);
    }

    public void exibirArmazem() {
        System.out.println("=== ARMAZÉM ===");
        if (produtosFabricados.isEmpty()) {
            System.out.println("Nenhum produto armazenado.");
            return;
        }
        for (Produto produto : produtosFabricados) {
            System.out.println("#" + produto.getId() + " — " + produto.getTipo()
                    + " — " + produto.getStatus());
            System.out.println("  " + produto.gerarRelatorioDiagnostico());
            System.out.println("  Circuito: " + produto.getConfiguracaoCircuito());
        }
        for (String tipo : new String[] {"STM32G", "STM32F", "STM32H"}) {
            System.out.println(tipo + " disponíveis: " + getQuantidadeArmazenada(tipo));
        }
        System.out.println("Total armazenado: " + produtosFabricados.size());
    }

    public void exibirEstoque() {
        System.out.println(materiaPrima.getNome() + ": " + materiaPrima.getQuantidade()
                + " " + materiaPrima.getUnidade());
        System.out.println("Mínimo para iniciar um ciclo: " + materiaPrima.getQuantidadeMinima());
        System.out.printf("Preço de compra por unidade: R$ %.2f%n", materiaPrima.getCustoPorUnidade());
    }

    public void exibirDemandas() {
        for (Demanda demanda : demandas) {
            String tipo = demanda.getTipoProduto();
            System.out.println(tipo + " [" + demanda.getStatus().getDescricao() + "]: " + demanda.getQuantidadeProdutos()
                    + " pendentes | Material mínimo, sem rejeições: "
                    + demanda.calcularMateriaPrimaNecessaria(obterConsumoPorUnidade(tipo)));
        }
    }

    public double getBudget() { return budget; }
    public long getTempoTotalProducao() { return tempoTotalProducao; }
    public int getTotalArmazenado() { return produtosFabricados.size(); }

    public int getQuantidadePendente(String tipoProduto) {
        return buscarDemanda(tipoProduto).getQuantidadeProdutos();
    }

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

    private double calcularCustoProducao() {
        double custo = 0.0;
        for (Maquina maquina : maquinas) {
            custo += maquina.getCustoOperacao();
        }
        return custo;
    }

    private Demanda buscarDemanda(String tipoProduto) {
        for (Demanda demanda : demandas) {
            if (demanda.getTipoProduto().equals(tipoProduto)) {
                return demanda;
            }
        }
        throw new IllegalArgumentException("Demanda não registrada para esse tipo de produto.");
    }

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

    private Produto criarProduto(String tipoProduto) {
        switch (tipoProduto) {
            case "STM32G": return new STM32G();
            case "STM32F": return new STM32F();
            case "STM32H": return new STM32H();
            default: throw new IllegalArgumentException("Tipo de produto desconhecido: " + tipoProduto);
        }
    }
    public GerenciadorProducao(Cenario cenario, EstrategiaProducao estrategia) {
        this(criarEstoque(cenario), cenario.getBudget());
        this.cenario = cenario;
        if (estrategia == null) throw new IllegalArgumentException("Informe uma estratégia.");
        this.estrategiaAtual = estrategia;
        for (Maquina maquina : maquinas) maquina.configurarCenario(cenario);
    }
    private static MateriaPrima criarEstoque(Cenario cenario) {
        if (cenario == null) throw new IllegalArgumentException("Informe o cenário.");
        return new MateriaPrima(1, "Silício", cenario.getEstoque(), "unidades", 10, 2);
    }
    public Cenario getCenario() { return cenario; }
    public void setEstrategia(EstrategiaProducao novaEstrategia) {
        if (novaEstrategia == null) throw new IllegalArgumentException("Informe uma estratégia.");
        estrategiaAtual = novaEstrategia;
    }
    public String getNomeEstrategia() {
        return estrategiaAtual == null ? "Seleção manual" : estrategiaAtual.getNomeEstrategia();
    }
    public void executarProximaProducao() {
        if (estrategiaAtual == null) throw new IllegalStateException("Selecione uma estratégia.");
        Demanda selecionada = estrategiaAtual.selecionarDemanda(Collections.unmodifiableList(demandas), budget);
        if (selecionada == null) {
            System.out.println("[AVISO] Nenhuma demanda elegível para a estratégia e orçamento atuais.");
            return;
        }
        if (!demandas.contains(selecionada) || selecionada.getStatus() != StatusDemanda.PENDENTE)
            throw new IllegalStateException("Estratégia retornou demanda inválida.");
        System.out.println("[SILICON FAB] Lote selecionado: " + selecionada.getTipoProduto());
        fabricarDemanda(selecionada.getTipoProduto());
    }
    public void gerarAuditoriaGeral() {
        List<Auditavel> componentes = new ArrayList<>();
        componentes.addAll(maquinas);
        componentes.addAll(produtosTentados);
        System.out.println("=== AUDITORIA DA SALA LIMPA (inclui rejeitados) ===");
        int intervencoes = 0;
        for (Auditavel componente : componentes) {
            System.out.println(componente.gerarRelatorioDiagnostico());
            if (componente.precisaManutencao()) intervencoes++;
        }
        System.out.println("Componentes que requerem intervenção: " + intervencoes);
    }
    public int getTotalTentativas() { return produtosTentados.size(); }
    public List<Demanda> getDemandas() { return Collections.unmodifiableList(demandas); }
    public List<Maquina> getMaquinas() { return Collections.unmodifiableList(maquinas); }
    public List<Produto> getProdutosFabricados() { return Collections.unmodifiableList(produtosFabricados); }
    public void setSeed(long seed) {
        for (int i = 0; i < maquinas.size(); i++) maquinas.get(i).setSeed(seed + i);
    }
}
