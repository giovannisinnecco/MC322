// Reúne os dados e comportamentos comuns a qualquer microcontrolador.
public abstract class Produto {
    // Guarda a identidade, o estado e as características da unidade.
    private final int id;
    private final String nome;
    private String status;
    private String configuracaoCircuito;
    private final int quantidadeMateriaPrimaPorUnidade;
    private final double qualidade;
    private double probabilidadeFalhaAcumulada;

    // Conta todas as unidades criadas pela fábrica.
    private static int totalProdutosFabricados = 0;

    // Cria uma unidade com seus parâmetros de fabricação.
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
        this.status = "Aguardando";
        this.configuracaoCircuito = "Não configurado";
        this.quantidadeMateriaPrimaPorUnidade = quantidadeMateriaPrimaPorUnidade;
        this.qualidade = qualidade;
        this.probabilidadeFalhaAcumulada = 0.0;
    }

    // Deixa cada modelo definir seu processamento, tempo e tipo.
    public abstract void processar();
    public abstract int calcularTempoProducao();
    public abstract String getTipo();

    // Libera a leitura dos dados sem expor os atributos.
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getStatus() { return status; }
    public String getConfiguracaoCircuito() { return configuracaoCircuito; }

    // Permite que cada modelo grave sua configuração de circuito.
    protected void configurarCircuito(String configuracaoCircuito) {
        if (configuracaoCircuito == null || configuracaoCircuito.trim().isEmpty()) {
            throw new IllegalArgumentException("A configuração do circuito deve ser informada.");
        }
        this.configuracaoCircuito = configuracaoCircuito;
    }

    // Atualiza o estado conforme o produto avança na linha.
    public void setStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("O estado do produto deve ser informado.");
        }
        this.status = status;
    }
    public int getQuantidadeMateriaPrimaPorUnidade() { return quantidadeMateriaPrimaPorUnidade; }
    public double getQualidade() { return qualidade; }

    // Soma o risco adquirido durante a fabricação.
    public void aumentarProbabilidadeFalha(double incremento) {
        if (!Double.isFinite(incremento) || incremento < 0.0 || incremento > 1.0) {
            throw new IllegalArgumentException("O incremento deve estar entre 0.0 e 1.0.");
        }
        this.probabilidadeFalhaAcumulada = Math.min(1.0,
                this.probabilidadeFalhaAcumulada + incremento);
    }

    public double getProbabilidadeFalhaAcumulada() { return probabilidadeFalhaAcumulada; }

    // Informa quantas unidades já foram criadas.
    public static int getTotalProdutosFabricados() { return totalProdutosFabricados; }
}
