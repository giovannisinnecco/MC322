/**
 * Guarda identidade, lote, qualidade exigida e risco de cada chip.
 * @author Giovanni Innecco e Rodrigo Soares
 */
public abstract class Produto implements Auditavel {
    private final int id;
    private final String nome;
    private StatusProduto status;
    private int lote;
    private PorcaoMateriaPrima origem;
    private String configuracaoCircuito;
    private final int quantidadeMateriaPrimaPorUnidade;
    private final double qualidade;
    private double probabilidadeFalhaAcumulada;

    // Conta também os chips que acabam rejeitados.
    private static int totalProdutosFabricados = 0;

    /**
     * Cria um chip aguardando processamento e atribui um ID único.
     * @param nome nome definido no código
     * @param quantidadeMateriaPrimaPorUnidade consumo positivo de silício por tentativa
     * @param qualidade exigência entre zero e um
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    public Produto(String nome, int quantidadeMateriaPrimaPorUnidade, double qualidade) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do produto deve ser informado.");
        }
        if (quantidadeMateriaPrimaPorUnidade <= 0) {
            throw new IllegalArgumentException("O consumo de matéria-prima deve ser positivo.");
        }
        if (!Double.isFinite(qualidade) || qualidade < 0.0 || qualidade > 1.0) {
            throw new IllegalArgumentException("A qualidade deve estar entre 0.0 e 1.0.");
        }
        this.id = ++totalProdutosFabricados;
        this.nome = nome;
        this.status = StatusProduto.AGUARDANDO;
        this.configuracaoCircuito = "Não configurado";
        this.quantidadeMateriaPrimaPorUnidade = quantidadeMateriaPrimaPorUnidade;
        this.qualidade = qualidade;
        this.probabilidadeFalhaAcumulada = 0.0;
    }

    /**
     * Executa a etapa de processamento deste componente.
     */
    public abstract void processar();
    /**
     * Consulta tempo simulado de uma tentativa, sem espera real.
     * @return tempo simulado de uma tentativa, sem espera real
     */
    public abstract int calcularTempoProducao();
    /**
     * Consulta identificação do modelo ou da etapa.
     * @return identificação do modelo ou da etapa
     */
    public abstract String getTipo();

    /**
     * Consulta identificador do objeto.
     * @return identificador do objeto
     */
    public int getId() { return id; }
    /**
     * Consulta nome definido no código.
     * @return nome definido no código
     */
    public String getNome() { return nome; }
    /**
     * Consulta estado atual.
     * @return estado atual
     */
    public StatusProduto getStatus() { return status; }
    /**
     * Consulta aplicação do circuito, ou indicação de que ainda não foi configurado.
     * @return aplicação do circuito, ou indicação de que ainda não foi configurado
     */
    public String getConfiguracaoCircuito() { return configuracaoCircuito; }

    /**
     * Registra a aplicação escolhida para o circuito.
     * @param configuracaoCircuito descrição não vazia da aplicação do circuito
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    protected void configurarCircuito(String configuracaoCircuito) {
        if (configuracaoCircuito == null || configuracaoCircuito.trim().isEmpty()) {
            throw new IllegalArgumentException("A configuração do circuito deve ser informada.");
        }
        this.configuracaoCircuito = configuracaoCircuito;
    }

    /**
     * Atualiza a etapa do chip. A máquina chamadora valida a ordem das etapas.
     * @param status estado não nulo do chip
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    public void setStatus(StatusProduto status) {
        if (status == null) {
            throw new IllegalArgumentException("O estado do produto deve ser informado.");
        }
        this.status = status;
    }
    /**
     * Consulta unidades de silício consumidas por tentativa.
     * @return unidades de silício consumidas por tentativa
     */
    public int getQuantidadeMateriaPrimaPorUnidade() { return quantidadeMateriaPrimaPorUnidade; }
    /**
     * Consulta exigência de qualidade entre zero e um.
     * @return exigência de qualidade entre zero e um
     */
    public double getQualidade() { return qualidade; }

    /**
     * Acrescenta risco ao chip, limitado a 100%.
     * @param incremento risco adicional entre zero e um
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    public void aumentarProbabilidadeFalha(double incremento) {
        if (!Double.isFinite(incremento) || incremento < 0.0 || incremento > 1.0) {
            throw new IllegalArgumentException("O incremento deve estar entre 0.0 e 1.0.");
        }
        this.probabilidadeFalhaAcumulada = Math.min(1.0,
                this.probabilidadeFalhaAcumulada + incremento);
    }

    /**
     * Consulta risco acumulado entre zero e um.
     * @return risco acumulado entre zero e um
     */
    public double getProbabilidadeFalhaAcumulada() { return probabilidadeFalhaAcumulada; }

    /**
     * Consulta instâncias criadas, incluindo chips rejeitados e tentativas desfeitas.
     * @return instâncias criadas, incluindo chips rejeitados e tentativas desfeitas
     */
    public static int getTotalProdutosFabricados() { return totalProdutosFabricados; }
    /**
     * Consulta número do lote, ou zero antes da atribuição.
     * @return número do lote, ou zero antes da atribuição
     */
    public int getLote() { return lote; }
    /**
     * Atribui um lote positivo uma única vez.
     * @param lote número positivo do lote
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    public void setLote(int lote) {
        if (lote <= 0 || this.lote != 0) throw new IllegalArgumentException("Lote inválido ou já atribuído.");
        this.lote = lote;
    }
    /**
     * Consulta porção recebida, ou null antes do abastecimento.
     * @return porção recebida, ou null antes do abastecimento
     */
    public PorcaoMateriaPrima getOrigem() { return origem; }
    /**
     * Vincula o silício ao chip antes do processamento, uma única vez.
     * @param porcao porção com o consumo exato do modelo
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     * @throws IllegalStateException se a operação contrariar o estado atual
     */
    public void registrarOrigem(PorcaoMateriaPrima porcao) {
        if (porcao == null || porcao.getQuantidade() != quantidadeMateriaPrimaPorUnidade)
            throw new IllegalArgumentException("Porção incompatível com o consumo do produto.");
        if (origem != null || status != StatusProduto.AGUARDANDO)
            throw new IllegalStateException("Origem já definida ou processamento iniciado.");
        origem = porcao;
    }
    // Quanto maior a qualidade exigida, menor a tolerância ao risco.
    /**
     * Verifica se o componente precisa de intervenção.
     * @return true se o componente requer intervenção
     */
    @Override
    public boolean precisaManutencao() {
        return status == StatusProduto.REJEITADO || qualidade * probabilidadeFalhaAcumulada >= 0.15;
    }
    /**
     * Reúne as condições atuais do componente para auditoria.
     * @return diagnóstico legível do componente
     */
    @Override
    public String gerarRelatorioDiagnostico() {
        return String.format("#%d %s | lote %d | %s | qualidade %.2f | risco %.2f%% | intervenção: %s",
                id, nome, lote, status, qualidade, 100 * probabilidadeFalhaAcumulada, precisaManutencao())
                + " | origem: " + (origem == null ? "não registrada" : origem.descrever());
    }
}
