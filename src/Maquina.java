import java.util.Random;

// Define o funcionamento comum das máquinas da linha.
public abstract class Maquina {
    // Guarda os dados operacionais e o gerador de falhas.
    private final String nome;
    private boolean ligada;
    private final int capacidadeMaxima;
    private final double probabilidadeFalha;
    private final double custoOperacao;
    private final Random random = new Random();

    // Cria uma máquina com capacidade, risco e custo definidos.
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
        this.ligada = false;
        this.capacidadeMaxima = capacidadeMaxima;
        this.probabilidadeFalha = probabilidadeFalha;
        this.custoOperacao = custoOperacao;
    }

    // Deixa cada máquina definir sua etapa de produção.
    public abstract void processar(Produto p);
    public abstract String getTipo();

    // Controla a energia e expõe os dados operacionais.
    public void ligar() { this.ligada = true; }
    public void desligar() { this.ligada = false; }
    public boolean estaLigada() { return ligada; }
    public String getNome() { return nome; }
    public int getCapacidadeMaxima() { return capacidadeMaxima; }
    public double getProbabilidadeFalha() { return probabilidadeFalha; }
    public double getCustoOperacao() { return custoOperacao; }

    // Sorteia um evento usando a chance recebida.
    protected boolean verificarFalha(double chanceFalha) {
        validarProbabilidade(chanceFalha);
        double sorteio = random.nextDouble();
        return sorteio < chanceFalha;
    }

    // Confere se a máquina pode receber o produto.
    protected void validarProcessamento(Produto produto, String statusEsperado) {
        if (!ligada) {
            throw new IllegalStateException("A máquina " + nome + " está desligada.");
        }
        if (produto == null) {
            throw new IllegalArgumentException("O produto deve ser informado.");
        }
        if (!statusEsperado.equals(produto.getStatus())) {
            throw new IllegalStateException("A máquina " + nome
                    + " exige um produto no estado " + statusEsperado + ".");
        }
    }

    // Mantém qualquer probabilidade dentro do intervalo válido.
    private void validarProbabilidade(double probabilidade) {
        if (!Double.isFinite(probabilidade) || probabilidade < 0.0 || probabilidade > 1.0) {
            throw new IllegalArgumentException("A probabilidade deve estar entre 0.0 e 1.0.");
        }
    }
}
