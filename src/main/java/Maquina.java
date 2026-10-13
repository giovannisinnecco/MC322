import java.util.Random;

/**
 * Controla estado, capacidade, custo e desgaste de uma etapa da linha.
 * @author Giovanni Innecco e Rodrigo Soares
 */
public abstract class Maquina implements Auditavel {
    private final String nome;
    private EstadoMaquina estado = EstadoMaquina.DESLIGADA;
    private double saude = 100, limiteManutencao = 30, desgasteMaximo = 1;
    private double fatorFalha = 1;
    private final int capacidadeMaxima;
    private final double probabilidadeFalha;
    private final double custoOperacao;
    private final Random random = new Random();

    /**
     * Cria uma máquina desligada, com saúde 100 e os limites informados.
     * @param nome nome definido no código
     * @param capacidadeMaxima capacidade positiva do equipamento
     * @param probabilidadeFalha chance básica entre zero e um
     * @param custoOperacao custo finito e não negativo em reais
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    public Maquina(String nome, int capacidadeMaxima, double probabilidadeFalha, double custoOperacao) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome da máquina deve ser informado.");
        }
        if (capacidadeMaxima <= 0) {
            throw new IllegalArgumentException("A capacidade deve ser positiva.");
        }
        validarProbabilidade(probabilidadeFalha);
        if (!Double.isFinite(custoOperacao) || custoOperacao < 0.0) {
            throw new IllegalArgumentException("O custo deve ser finito e não negativo.");
        }
        this.nome = nome;

        this.capacidadeMaxima = capacidadeMaxima;
        this.probabilidadeFalha = probabilidadeFalha;
        this.custoOperacao = custoOperacao;
    }

    /**
     * Executa a etapa de processamento deste componente.
     * @param p chip recebido pela etapa
     * @throws EquipamentoIndisponivelException se o estado, a etapa ou a capacidade impedir a operação
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    public abstract void processar(Produto p);
    /**
     * Consulta identificação do modelo ou da etapa.
     * @return identificação do modelo ou da etapa
     */
    public abstract String getTipo();

    /**
     * Habilita o equipamento para operar.
     * @throws EquipamentoIndisponivelException se o estado, a etapa ou a capacidade impedir a operação
     */
    public void ligar() {
        if (estado == EstadoMaquina.QUEBRADA) throw new EquipamentoIndisponivelException("Máquina quebrada: " + nome);
        estado = EstadoMaquina.LIGADA;
    }
    /**
     * Interrompe a operação do equipamento.
     */
    public void desligar() {
        if (estado != EstadoMaquina.QUEBRADA) estado = EstadoMaquina.DESLIGADA;
    }
    /**
     * Consulta true se a máquina está ligada.
     * @return true se a máquina está ligada
     */
    public boolean estaLigada() { return estado == EstadoMaquina.LIGADA; }
    /**
     * Consulta nome definido no código.
     * @return nome definido no código
     */
    public String getNome() { return nome; }
    /**
     * Consulta capacidade máxima do equipamento.
     * @return capacidade máxima do equipamento
     */
    public int getCapacidadeMaxima() { return capacidadeMaxima; }
    // Com metade da saúde, o risco dobra, até chegar a 100%.
    /**
     * Consulta risco efetivo, inverso à saúde e limitado a um.
     * @return risco efetivo, inverso à saúde e limitado a um
     */
    public double getProbabilidadeFalha() {
        if (saude == 0) return 1;
        return Math.min(1, probabilidadeFalha * fatorFalha * 100 / saude);
    }
    /**
     * Verifica se há capacidade para a quantidade informada.
     * @param quantidade quantidade solicitada
     * @return true se a quantidade cabe
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    public boolean verificarCapacidade(double quantidade) {
        if (!Double.isFinite(quantidade) || quantidade < 0)
            throw new IllegalArgumentException("Carga inválida.");
        return quantidade <= capacidadeMaxima;
    }
    /**
     * Consulta custo em reais por tentativa nesta etapa.
     * @return custo em reais por tentativa nesta etapa
     */
    public double getCustoOperacao() { return custoOperacao; }

    /**
     * Sorteia uma falha usando a probabilidade recebida.
     * @param chanceFalha probabilidade entre zero e um
     * @return true se o sorteio indica falha
     * @throws IllegalArgumentException se a probabilidade não for finita ou estiver fora de zero a um
     */
    protected boolean verificarFalha(double chanceFalha) {
        validarProbabilidade(chanceFalha);
        double sorteio = random.nextDouble();
        return sorteio < chanceFalha;
    }

    /**
     * Confere estado, carga e etapa antes de alterar o chip.
     * @param produto chip recebido pela etapa
     * @param statusEsperado etapa exigida na entrada da máquina
     * @throws EquipamentoIndisponivelException se o estado, a etapa ou a capacidade impedir a operação
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    protected void validarProcessamento(Produto produto, StatusProduto statusEsperado) {
        if (!estaLigada()) {
            throw new EquipamentoIndisponivelException("A máquina " + nome + " está desligada.");
        }
        if (produto == null) {
            throw new IllegalArgumentException("O produto deve ser informado.");
        }
        if (!verificarCapacidade(produto.getQuantidadeMateriaPrimaPorUnidade())) {
            throw new EquipamentoIndisponivelException("Carga excede a capacidade de " + nome + ".");
        }
        if (statusEsperado != produto.getStatus()) {
            throw new EquipamentoIndisponivelException("A máquina " + nome
                    + " exige um produto no estado " + statusEsperado + ".");
        }
    }

    /**
     * Rejeita valores fora do intervalo de zero a um.
     * @param probabilidade valor finito entre zero e um
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    private void validarProbabilidade(double probabilidade) {
        if (!Double.isFinite(probabilidade) || probabilidade < 0.0 || probabilidade > 1.0) {
            throw new IllegalArgumentException("A probabilidade deve estar entre 0.0 e 1.0.");
        }
    }
    /**
     * Aplica o fator de falha e o desgaste do cenário.
     * @param cenario configuração não nula do turno
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    public void configurarCenario(Cenario cenario) {
        if (cenario == null) throw new IllegalArgumentException("Informe o cenário.");
        fatorFalha = cenario.getFatorFalha();
        desgasteMaximo = cenario.getDesgaste();
    }
    /**
     * Define a semente dos sorteios para repetir uma simulação.
     * @param seed semente inicial dos sorteios
     */
    public void setSeed(long seed) { random.setSeed(seed); }
    /**
     * Desconta um desgaste aleatório da saúde após o uso.
     */
    protected void registrarUso() { setSaude(Math.max(0, saude - random.nextDouble() * desgasteMaximo)); }
    /**
     * Consulta saúde entre zero e cem.
     * @return saúde entre zero e cem
     */
    public double getSaude() { return saude; }
    /**
     * Consulta estado operacional da máquina.
     * @return estado operacional da máquina
     */
    public EstadoMaquina getEstado() { return estado; }
    /**
     * Ajusta a saúde. Zero quebra a máquina; restaurar uma quebrada a deixa desligada.
     * @param valor valor a validar ou aplicar
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    public void setSaude(double valor) {
        if (!Double.isFinite(valor) || valor < 0 || valor > 100) throw new IllegalArgumentException("Saúde inválida.");
        saude = valor;
        if (saude == 0) estado = EstadoMaquina.QUEBRADA;
        else if (estado == EstadoMaquina.QUEBRADA) estado = EstadoMaquina.DESLIGADA;
    }
    /**
     * Define a saúde abaixo da qual a máquina pede manutenção.
     * @param valor valor a validar ou aplicar
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    public void setLimiteManutencao(double valor) {
        if (!Double.isFinite(valor) || valor <= 0 || valor > 100) throw new IllegalArgumentException("Limiar inválido.");
        limiteManutencao = valor;
    }
    /**
     * Verifica se o componente precisa de intervenção.
     * @return true se o componente requer intervenção
     */
    @Override
    public boolean precisaManutencao() { return saude < limiteManutencao; }
    /**
     * Reúne as condições atuais do componente para auditoria.
     * @return diagnóstico legível do componente
     */
    @Override
    public String gerarRelatorioDiagnostico() {
        return String.format("%s | %s | saúde %.2f | falha %.2f%% | manutenção: %s",
                nome, estado, saude, 100 * getProbabilidadeFalha(), precisaManutencao());
    }
}
