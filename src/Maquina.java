import java.util.Random;

public abstract class Maquina implements Auditavel {
    private final String nome;
    private EstadoMaquina estado = EstadoMaquina.DESLIGADA;
    private double saude = 100, limiteManutencao = 30, desgasteMaximo = 1;
    private double fatorFalha = 1;
    private final int capacidadeMaxima;
    private final double probabilidadeFalha;
    private final double custoOperacao;
    private final Random random = new Random();

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

    public abstract void processar(Produto p);
    public abstract String getTipo();

    public void ligar() {
        if (estado == EstadoMaquina.QUEBRADA) throw new IllegalStateException("Máquina quebrada: " + nome);
        estado = EstadoMaquina.LIGADA;
    }
    public void desligar() {
        if (estado != EstadoMaquina.QUEBRADA) estado = EstadoMaquina.DESLIGADA;
    }
    public boolean estaLigada() { return estado == EstadoMaquina.LIGADA; }
    public String getNome() { return nome; }
    public int getCapacidadeMaxima() { return capacidadeMaxima; }
    // Com metade da saúde, o risco dobra, até chegar a 100%.
    public double getProbabilidadeFalha() {
        if (saude == 0) return 1;
        return Math.min(1, probabilidadeFalha * fatorFalha * 100 / saude);
    }
    public boolean verificarCapacidade(double quantidade) {
        if (!Double.isFinite(quantidade) || quantidade < 0)
            throw new IllegalArgumentException("Carga inválida.");
        return quantidade <= capacidadeMaxima;
    }
    public double getCustoOperacao() { return custoOperacao; }

    protected boolean verificarFalha(double chanceFalha) {
        validarProbabilidade(chanceFalha);
        double sorteio = random.nextDouble();
        return sorteio < chanceFalha;
    }

    protected void validarProcessamento(Produto produto, StatusProduto statusEsperado) {
        if (!estaLigada()) {
            throw new IllegalStateException("A máquina " + nome + " está desligada.");
        }
        if (produto == null) {
            throw new IllegalArgumentException("O produto deve ser informado.");
        }
        if (!verificarCapacidade(produto.getQuantidadeMateriaPrimaPorUnidade())) {
            throw new IllegalStateException("Carga excede a capacidade de " + nome + ".");
        }
        if (statusEsperado != produto.getStatus()) {
            throw new IllegalStateException("A máquina " + nome
                    + " exige um produto no estado " + statusEsperado + ".");
        }
    }

    private void validarProbabilidade(double probabilidade) {
        if (!Double.isFinite(probabilidade) || probabilidade < 0.0 || probabilidade > 1.0) {
            throw new IllegalArgumentException("A probabilidade deve estar entre 0.0 e 1.0.");
        }
    }
    public void configurarCenario(Cenario cenario) {
        if (cenario == null) throw new IllegalArgumentException("Informe o cenário.");
        fatorFalha = cenario.getFatorFalha();
        desgasteMaximo = cenario.getDesgaste();
    }
    public void setSeed(long seed) { random.setSeed(seed); }
    protected void registrarUso() { setSaude(Math.max(0, saude - random.nextDouble() * desgasteMaximo)); }
    public double getSaude() { return saude; }
    public EstadoMaquina getEstado() { return estado; }
    public void setSaude(double valor) {
        if (!Double.isFinite(valor) || valor < 0 || valor > 100) throw new IllegalArgumentException("Saúde inválida.");
        saude = valor;
        if (saude == 0) estado = EstadoMaquina.QUEBRADA;
        else if (estado == EstadoMaquina.QUEBRADA) estado = EstadoMaquina.DESLIGADA;
    }
    public void setLimiteManutencao(double valor) {
        if (!Double.isFinite(valor) || valor <= 0 || valor > 100) throw new IllegalArgumentException("Limiar inválido.");
        limiteManutencao = valor;
    }
    @Override public boolean precisaManutencao() { return saude < limiteManutencao; }
    @Override public String gerarRelatorioDiagnostico() {
        return String.format("%s | %s | saúde %.2f | falha %.2f%% | manutenção: %s",
                nome, estado, saude, 100 * getProbabilidadeFalha(), precisaManutencao());
    }
}
