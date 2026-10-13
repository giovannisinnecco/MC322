import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Cuida dos pedidos e dos recursos da fábrica. Cada tentativa é confirmada por inteiro; chips já
 * concluídos são preservados após uma interrupção.
 * @author Giovanni Innecco e Rodrigo Soares
 */
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

    /**
     * Abre uma linha manual com as três máquinas padrão.
     * @param materiaPrima estoque utilizado pela linha
     * @param budget orçamento disponível em reais
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    public GerenciadorProducao(MateriaPrima materiaPrima, double budget) {
        this(materiaPrima, budget, new MaquinaFotolitografia(),
                new MaquinaEncapsulamento(), new EstacaoInspecao());
    }

    /**
     * Abre uma linha manual com o estoque, orçamento e máquinas recebidos.
     * @param materiaPrima estoque utilizado pela linha
     * @param budget orçamento disponível em reais
     * @param fotolitografia primeira máquina da linha
     * @param encapsulamento segunda máquina da linha
     * @param inspecao estação final da linha
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
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

    /**
     * Cadastra um pedido por modelo, usando o custo da linha atual.
     * @param tipoProduto modelo solicitado
     * @param quantidadeProdutos quantidade não negativa de chips aprovados solicitados
     * @throws DemandaInvalidaException se o modelo, o pedido ou sua transição não forem válidos
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    public void registrarDemanda(String tipoProduto, int quantidadeProdutos) {
        obterConsumoPorUnidade(tipoProduto);
        for (Demanda demanda : demandas) {
            if (demanda.getTipoProduto().equals(tipoProduto)) {
                throw new DemandaInvalidaException("Já existe demanda para " + tipoProduto + ".");
            }
        }
        demandas.add(new Demanda(tipoProduto, quantidadeProdutos,
                obterConsumoPorUnidade(tipoProduto), calcularCustoProducao()));
    }

    /**
     * Atualiza o saldo; pedidos reabertos voltam ao fim da fila.
     * @param tipoProduto modelo solicitado
     * @param novaQuantidade novo saldo não negativo
     * @throws DemandaInvalidaException se o modelo, o pedido ou sua transição não forem válidos
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
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

    /**
     * Fabrica até concluir ou interromper o pedido. Falta de recursos cancela o saldo; quebra ou
     * erro de ciclo o deixa pendente. Tentativas concluídas são preservadas.
     * @param tipoProduto modelo solicitado
     * @throws DemandaInvalidaException se o modelo, o pedido ou sua transição não forem válidos
     * @throws EquipamentoIndisponivelException se o estado, a etapa ou a capacidade impedir a operação
     * @throws FalhaProducaoException se uma etapa falhar inesperadamente; a tentativa é desfeita
     * @throws RecursoInsuficienteException se faltar silício ou orçamento para a operação
     */
    public void fabricarDemanda(String tipoProduto) {
        Demanda demanda = buscarDemanda(tipoProduto);
        if (demanda.getStatus() != StatusDemanda.PENDENTE) {
            throw new DemandaInvalidaException("Demanda não elegível; atualize o pedido de " + tipoProduto + ".");
        }

        for (Maquina maquina : maquinas) {
            if (maquina.getEstado() == EstadoMaquina.QUEBRADA) {
                throw new EquipamentoIndisponivelException("Linha parada: " + maquina.getNome() + " quebrada.");
            }
        }
        int consumoPorUnidade = obterConsumoPorUnidade(tipoProduto);
        if (!esteira.verificarCarga(consumoPorUnidade)) {
            throw new EquipamentoIndisponivelException("Carga excede a capacidade da esteira; pedido permanece pendente.");
        }
        for (Maquina maquina : maquinas) {
            if (!maquina.verificarCapacidade(consumoPorUnidade)) {
                throw new EquipamentoIndisponivelException("Carga excede a capacidade de " + maquina.getNome()
                        + "; pedido permanece pendente.");
            }
        }
        double custoCiclo = calcularCustoProducao();
        validarRecursos(demanda, consumoPorUnidade, custoCiclo);
        demanda.iniciar();
        int lote = ++ultimoLote;
        long tentativas = 0;
        int aprovados = 0;
        long tempoInicial = tempoTotalProducao;

        try {
            esteira.ligar();
            for (Maquina maquina : maquinas) {
                maquina.ligar();
            }
            while (demanda.getStatus() == StatusDemanda.EM_PRODUCAO) {
                validarRecursos(demanda, consumoPorUnidade, custoCiclo);

                boolean quebrada = false;
                for (Maquina maquina : maquinas) quebrada |= maquina.getEstado() == EstadoMaquina.QUEBRADA;
                if (quebrada) {
                    demanda.pausar();
                    throw new EquipamentoIndisponivelException("Linha parada por desgaste. Demanda volta a pendente.");
                }
                Produto produto = produzirUnidade(tipoProduto, lote);
                tentativas++;

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
            esteira.esvaziar();
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

    /**
     * Cancela o saldo antes de debitar uma tentativa sem recursos.
     * @param demanda pedido consultado ou em produção
     * @param consumo consumo positivo por unidade
     * @param custo custo operacional por tentativa
     * @throws RecursoInsuficienteException se faltar silício ou orçamento para a operação
     */
    private void validarRecursos(Demanda demanda, int consumo, double custo) {
        if (!materiaPrima.verificarDisponibilidade(consumo)) {
            demanda.cancelar();
            throw new RecursoInsuficienteException("Demanda cancelada: silício insuficiente ou abaixo do mínimo. "
                    + "Reponha o estoque e atualize o pedido. As tentativas concluídas foram mantidas.");
        }
        if (budget < custo) {
            demanda.cancelar();
            throw new RecursoInsuficienteException("Demanda cancelada: orçamento insuficiente para outra tentativa. "
                    + "As tentativas concluídas foram mantidas.");
        }
    }

    /**
     * Executa um ciclo e desfaz suas alterações se uma etapa lançar uma exceção. IDs e sorteios
     * já usados não são reutilizados.
     * @param tipoProduto modelo solicitado
     * @param lote número positivo do lote
     * @return chip aprovado ou rejeitado ao fim do ciclo
     * @throws FalhaProducaoException se uma etapa falhar inesperadamente; a tentativa é desfeita
     * @throws PlantaException se uma regra da linha impedir o ciclo
     */
    private Produto produzirUnidade(String tipoProduto, int lote) {
        double estoqueAnterior = materiaPrima.getQuantidade();
        double budgetAnterior = budget;
        double[] saudes = new double[maquinas.size()];
        for (int i = 0; i < maquinas.size(); i++) saudes[i] = maquinas.get(i).getSaude();
        EstacaoInspecao inspecao = (EstacaoInspecao) maquinas.get(2);
        int inspecionados = inspecao.getTotalInspecionados();
        Produto produto = criarProduto(tipoProduto);
        produto.setLote(lote);
        if (cenario != null) produto.aumentarProbabilidadeFalha(cenario.getRiscoProduto());
        try {
            PorcaoMateriaPrima porcao = materiaPrima.retirarPorcao(produto.getQuantidadeMateriaPrimaPorUnidade());
            esteira.adicionarMateriaPrima(porcao);
            produto.registrarOrigem(esteira.removerMateriaPrima());
            for (Maquina maquina : maquinas) {
                esteira.adicionarItem(produto);
                maquina.processar(esteira.removerItem());
                budget -= maquina.getCustoOperacao();
            }
            if (produto.getStatus() != StatusProduto.APROVADO && produto.getStatus() != StatusProduto.REJEITADO)
                throw new IllegalStateException("A linha terminou sem um resultado de inspeção.");
        } catch (RuntimeException erro) {
            // Desfaz só este chip; os anteriores já terminaram a linha.
            materiaPrima.restaurarQuantidade(estoqueAnterior);
            budget = budgetAnterior;
            for (int i = 0; i < maquinas.size(); i++) maquinas.get(i).setSaude(saudes[i]);
            inspecao.restaurarTotalInspecionados(inspecionados);
            esteira.esvaziar();
            if (erro instanceof PlantaException falha) throw falha;
            throw new FalhaProducaoException("Falha inesperada na linha. A tentativa foi desfeita; "
                    + "o pedido continua pendente. Motivo: " + erro.getClass().getSimpleName()
                    + " - " + erro.getMessage(), erro);
        }
        produtosTentados.add(produto);
        tempoTotalProducao += produto.calcularTempoProducao();
        System.out.println("[ESTEIRA] " + produto.getOrigem().descrever()
                + " transportado para o chip #" + produto.getId());
        return produto;
    }

    /**
     * Valida a compra, repõe o silício e debita o preço total.
     * @param quantidade quantidade solicitada
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     * @throws RecursoInsuficienteException se faltar silício ou orçamento para a operação
     */
    public void comprarMateriaPrima(double quantidade) {
        if (!Double.isFinite(quantidade) || quantidade <= 0.0) {
            throw new IllegalArgumentException("A quantidade de compra deve ser finita e positiva.");
        }
        double custoCompra = quantidade * materiaPrima.getCustoPorUnidade();
        if (!Double.isFinite(custoCompra) || custoCompra <= 0.0) {
            throw new IllegalArgumentException("O custo calculado da compra é inválido.");
        }
        if (custoCompra > budget) {
            throw new RecursoInsuficienteException("Orçamento insuficiente para a compra; reduza a quantidade.");
        }
        materiaPrima.adicionarEstoque(quantidade);
        budget -= custoCompra;
    }

    /**
     * Mostra o orçamento disponível para o turno.
     */
    public void exibirBudget() {
        System.out.printf("Orçamento disponível: R$ %.2f%n", budget);
    }

    /**
     * Lista chips aprovados com origem, lote, qualidade e risco.
     */
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

    /**
     * Mostra quantidade, mínimo e preço do silício bruto.
     */
    public void exibirEstoque() {
        System.out.println(materiaPrima.getNome() + ": " + materiaPrima.getQuantidade()
                + " " + materiaPrima.getUnidade());
        System.out.println("Mínimo para iniciar um ciclo: " + materiaPrima.getQuantidadeMinima());
        System.out.printf("Preço de compra por unidade: R$ %.2f%n", materiaPrima.getCustoPorUnidade());
    }

    /**
     * Mostra status, saldo e material estimado dos pedidos.
     */
    public void exibirDemandas() {
        for (Demanda demanda : demandas) {
            String tipo = demanda.getTipoProduto();
            System.out.println(tipo + " [" + demanda.getStatus().getDescricao() + "]: " + demanda.getQuantidadeProdutos()
                    + " pendentes | Material mínimo, sem rejeições: "
                    + demanda.calcularMateriaPrimaNecessaria(obterConsumoPorUnidade(tipo)));
        }
    }

    /**
     * Consulta orçamento em reais.
     * @return orçamento em reais
     */
    public double getBudget() { return budget; }
    /**
     * Consulta tempo das tentativas confirmadas, incluindo rejeições.
     * @return tempo das tentativas confirmadas, incluindo rejeições
     */
    public long getTempoTotalProducao() { return tempoTotalProducao; }
    /**
     * Consulta quantidade de chips aprovados.
     * @return quantidade de chips aprovados
     */
    public int getTotalArmazenado() { return produtosFabricados.size(); }

    /**
     * Consulta saldo de chips aprovados ainda solicitado.
     * @param tipoProduto modelo solicitado
     * @return saldo de chips aprovados ainda solicitado
     * @throws DemandaInvalidaException se o modelo, o pedido ou sua transição não forem válidos
     */
    public int getQuantidadePendente(String tipoProduto) {
        return buscarDemanda(tipoProduto).getQuantidadeProdutos();
    }

    /**
     * Consulta quantidade aprovada do modelo.
     * @param tipoProduto modelo solicitado
     * @return quantidade aprovada do modelo
     * @throws DemandaInvalidaException se o modelo, o pedido ou sua transição não forem válidos
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
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

    /**
     * Soma os custos das três etapas para uma tentativa.
     * @return custo operacional de uma tentativa completa
     */
    private double calcularCustoProducao() {
        double custo = 0.0;
        for (Maquina maquina : maquinas) {
            custo += maquina.getCustoOperacao();
        }
        return custo;
    }

    /**
     * Localiza o pedido cadastrado para o modelo.
     * @param tipoProduto modelo solicitado
     * @return pedido encontrado
     * @throws DemandaInvalidaException se o modelo, o pedido ou sua transição não forem válidos
     */
    private Demanda buscarDemanda(String tipoProduto) {
        for (Demanda demanda : demandas) {
            if (demanda.getTipoProduto().equals(tipoProduto)) {
                return demanda;
            }
        }
        throw new DemandaInvalidaException("Demanda não registrada para esse tipo de produto.");
    }

    /**
     * Consulta o consumo fixo de silício do modelo.
     * @param tipoProduto modelo solicitado
     * @return consumo do modelo por tentativa
     * @throws DemandaInvalidaException se o modelo, o pedido ou sua transição não forem válidos
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    private int obterConsumoPorUnidade(String tipoProduto) {
        if (tipoProduto == null) {
            throw new IllegalArgumentException("O tipo do produto deve ser informado.");
        }
        switch (tipoProduto) {
            case "STM32G": return STM32G.CONSUMO_MATERIA_PRIMA;
            case "STM32F": return STM32F.CONSUMO_MATERIA_PRIMA;
            case "STM32H": return STM32H.CONSUMO_MATERIA_PRIMA;
            default: throw new DemandaInvalidaException("Tipo de produto desconhecido: " + tipoProduto);
        }
    }

    /**
     * Cria um chip do modelo escolhido.
     * @param tipoProduto modelo solicitado
     * @return nova instância do modelo
     * @throws DemandaInvalidaException se o modelo, o pedido ou sua transição não forem válidos
     */
    private Produto criarProduto(String tipoProduto) {
        switch (tipoProduto) {
            case "STM32G": return new STM32G();
            case "STM32F": return new STM32F();
            case "STM32H": return new STM32H();
            default: throw new DemandaInvalidaException("Tipo de produto desconhecido: " + tipoProduto);
        }
    }
    /**
     * Abre a fábrica com os recursos do cenário e a estratégia escolhida.
     * @param cenario configuração não nula do turno
     * @param estrategia regra inicial de seleção
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    public GerenciadorProducao(Cenario cenario, EstrategiaProducao estrategia) {
        this(criarEstoque(cenario), cenario.getBudget());
        this.cenario = cenario;
        if (estrategia == null) throw new IllegalArgumentException("Informe uma estratégia.");
        this.estrategiaAtual = estrategia;
        for (Maquina maquina : maquinas) maquina.configurarCenario(cenario);
    }
    /**
     * Abre o estoque de silício definido pelo cenário.
     * @param cenario configuração não nula do turno
     * @return estoque inicial do cenário
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    private static MateriaPrima criarEstoque(Cenario cenario) {
        if (cenario == null) throw new IllegalArgumentException("Informe o cenário.");
        return new MateriaPrima(1, "Silício", cenario.getEstoque(), "unidades", 10, 2);
    }
    /**
     * Consulta cenário ativo, ou null nos construtores manuais.
     * @return cenário ativo, ou null nos construtores manuais
     */
    public Cenario getCenario() { return cenario; }
    /**
     * Troca a regra que escolherá o próximo pedido.
     * @param novaEstrategia nova regra não nula
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    public void setEstrategia(EstrategiaProducao novaEstrategia) {
        if (novaEstrategia == null) throw new IllegalArgumentException("Informe uma estratégia.");
        estrategiaAtual = novaEstrategia;
    }
    /**
     * Consulta nome para o menu.
     * @return nome para o menu
     */
    public String getNomeEstrategia() {
        return estrategiaAtual == null ? "Seleção manual" : estrategiaAtual.getNomeEstrategia();
    }
    /**
     * Delega a seleção à estratégia e usa o mesmo fluxo da produção manual.
     * @throws DemandaInvalidaException se o modelo, o pedido ou sua transição não forem válidos
     * @throws PlantaException se a seleção ou a fabricação não puder ser realizada
     */
    public void executarProximaProducao() {
        if (estrategiaAtual == null) throw new DemandaInvalidaException("Selecione uma estratégia.");
        Demanda selecionada = estrategiaAtual.selecionarDemanda(Collections.unmodifiableList(demandas), budget);
        if (selecionada == null) {
            throw new DemandaInvalidaException("Nenhuma demanda elegível para a estratégia e orçamento atuais.");
        }
        if (!demandas.contains(selecionada) || selecionada.getStatus() != StatusDemanda.PENDENTE)
            throw new DemandaInvalidaException("Estratégia retornou demanda inválida.");
        System.out.println("[SILICON FAB] Lote selecionado: " + selecionada.getTipoProduto());
        fabricarDemanda(selecionada.getTipoProduto());
    }
    /**
     * Consulta máquinas e chips polimorficamente, incluindo os rejeitados.
     */
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
    /**
     * Consulta ciclos confirmados, incluindo rejeições.
     * @return ciclos confirmados, incluindo rejeições
     */
    public int getTotalTentativas() { return produtosTentados.size(); }
    /**
     * Consulta lista sem alteração estrutural; os pedidos continuam mutáveis.
     * @return lista sem alteração estrutural; os pedidos continuam mutáveis
     */
    public List<Demanda> getDemandas() { return Collections.unmodifiableList(demandas); }
    /**
     * Consulta lista sem alteração estrutural; as máquinas continuam mutáveis.
     * @return lista sem alteração estrutural; as máquinas continuam mutáveis
     */
    public List<Maquina> getMaquinas() { return Collections.unmodifiableList(maquinas); }
    /**
     * Consulta lista sem alteração estrutural; os chips continuam mutáveis.
     * @return lista sem alteração estrutural; os chips continuam mutáveis
     */
    public List<Produto> getProdutosFabricados() { return Collections.unmodifiableList(produtosFabricados); }
    /**
     * Define a semente dos sorteios para repetir uma simulação.
     * @param seed semente inicial dos sorteios
     */
    public void setSeed(long seed) {
        for (int i = 0; i < maquinas.size(); i++) maquinas.get(i).setSeed(seed + i);
    }
}
