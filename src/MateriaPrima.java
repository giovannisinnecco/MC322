// Controla o estoque e o preço do silício.
public class MateriaPrima {

    // Guarda a identificação e os valores atuais do estoque.
    private final int id;
    private final String nome;
    private double quantidade;
    private final String unidade;
    private final double quantidadeMinima;
    private final double custoPorUnidade;

    // Cria a matéria-prima com estoque e preço iniciais.
    public MateriaPrima(int id, String nome, double quantidade, String unidade,
                        double quantidadeMinima, double custoPorUnidade) {
        if (id <= 0) {
            throw new IllegalArgumentException("O ID da matéria-prima deve ser positivo.");
        }
        if (nome == null || nome.trim().isEmpty() || unidade == null || unidade.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome e unidade devem ser informados.");
        }
        validarQuantidadeNaoNegativa(quantidade);
        validarQuantidadeNaoNegativa(quantidadeMinima);
        if (!Double.isFinite(custoPorUnidade) || custoPorUnidade <= 0.0) {
            throw new IllegalArgumentException("O custo por unidade deve ser finito e positivo.");
        }
        this.id = id;
        this.nome = nome;
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.quantidadeMinima = quantidadeMinima;
        this.custoPorUnidade = custoPorUnidade;
    }

    // Confere se o estoque atende a próxima operação.
    public boolean verificarDisponibilidade(double quantidadeNecessaria) {
        validarQuantidadeNaoNegativa(quantidadeNecessaria);
        return quantidade >= quantidadeNecessaria;
    }

    // Retira material quando uma unidade começa a ser produzida.
    public void consumir(double quantidadeNecessaria) {
        validarQuantidadePositiva(quantidadeNecessaria);
        if (!verificarDisponibilidade(quantidadeNecessaria)) {
            throw new IllegalStateException("Estoque insuficiente de " + nome + ".");
        }
        quantidade -= quantidadeNecessaria;
    }

    // Acrescenta ao estoque o material comprado.
    public void adicionarEstoque(double quantidadeAdicional) {
        validarQuantidadePositiva(quantidadeAdicional);
        double novaQuantidade = quantidade + quantidadeAdicional;
        validarQuantidadeNaoNegativa(novaQuantidade);
        quantidade = novaQuantidade;
    }

    // Aceita apenas quantidades finitas e não negativas.
    private void validarQuantidadeNaoNegativa(double valor) {
        if (!Double.isFinite(valor) || valor < 0.0) {
            throw new IllegalArgumentException("A quantidade deve ser finita e não negativa.");
        }
    }

    // Exige um valor positivo para movimentar o estoque.
    private void validarQuantidadePositiva(double valor) {
        validarQuantidadeNaoNegativa(valor);
        if (valor == 0.0) {
            throw new IllegalArgumentException("A quantidade movimentada deve ser positiva.");
        }
    }

    // Libera a leitura dos dados do estoque.
    public int getId() { return id; }
    public String getNome() { return nome; }
    public double getQuantidade() { return quantidade; }
    public String getUnidade() { return unidade; }
    public double getQuantidadeMinima() { return quantidadeMinima; }
    public double getCustoPorUnidade() { return custoPorUnidade; }
}
