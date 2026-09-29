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

    public abstract void processar();
    public abstract int calcularTempoProducao();
    public abstract String getTipo();

    public int getId() { return id; }
    public String getNome() { return nome; }
    public StatusProduto getStatus() { return status; }
    public String getConfiguracaoCircuito() { return configuracaoCircuito; }

    protected void configurarCircuito(String configuracaoCircuito) {
        if (configuracaoCircuito == null || configuracaoCircuito.trim().isEmpty()) {
            throw new IllegalArgumentException("A configuração do circuito deve ser informada.");
        }
        this.configuracaoCircuito = configuracaoCircuito;
    }

    public void setStatus(StatusProduto status) {
        if (status == null) {
            throw new IllegalArgumentException("O estado do produto deve ser informado.");
        }
        this.status = status;
    }
    public int getQuantidadeMateriaPrimaPorUnidade() { return quantidadeMateriaPrimaPorUnidade; }
    public double getQualidade() { return qualidade; }

    public void aumentarProbabilidadeFalha(double incremento) {
        if (!Double.isFinite(incremento) || incremento < 0.0 || incremento > 1.0) {
            throw new IllegalArgumentException("O incremento deve estar entre 0.0 e 1.0.");
        }
        this.probabilidadeFalhaAcumulada = Math.min(1.0,
                this.probabilidadeFalhaAcumulada + incremento);
    }

    public double getProbabilidadeFalhaAcumulada() { return probabilidadeFalhaAcumulada; }

    public static int getTotalProdutosFabricados() { return totalProdutosFabricados; }
    public int getLote() { return lote; }
    public void setLote(int lote) {
        if (lote <= 0 || this.lote != 0) throw new IllegalArgumentException("Lote inválido ou já atribuído.");
        this.lote = lote;
    }
    public PorcaoMateriaPrima getOrigem() { return origem; }
    public void registrarOrigem(PorcaoMateriaPrima porcao) {
        if (porcao == null || porcao.getQuantidade() != quantidadeMateriaPrimaPorUnidade)
            throw new IllegalArgumentException("Porção incompatível com o consumo do produto.");
        if (origem != null || status != StatusProduto.AGUARDANDO)
            throw new IllegalStateException("Origem já definida ou processamento iniciado.");
        origem = porcao;
    }
    // Quanto maior a qualidade exigida, menor a tolerância ao risco.
    @Override public boolean precisaManutencao() {
        return status == StatusProduto.REJEITADO || qualidade * probabilidadeFalhaAcumulada >= 0.15;
    }
    @Override public String gerarRelatorioDiagnostico() {
        return String.format("#%d %s | lote %d | %s | qualidade %.2f | risco %.2f%% | intervenção: %s",
                id, nome, lote, status, qualidade, 100 * probabilidadeFalhaAcumulada, precisaManutencao())
                + " | origem: " + (origem == null ? "não registrada" : origem.descrever());
    }
}
