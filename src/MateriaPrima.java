public class MateriaPrima {

    private final int id;
    private final String nome;
    private double quantidade;
    private final String unidade;
    private final double quantidadeMinima;
    private final double custoPorUnidade;

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

    public boolean verificarDisponibilidade(double quantidadeNecessaria) {
        validarQuantidadeNaoNegativa(quantidadeNecessaria);
        // O mínimo precisa existir antes de começar o ciclo.
        return quantidade >= quantidadeNecessaria
                && (quantidadeNecessaria == 0 || quantidade >= quantidadeMinima);
    }

    public void consumir(double quantidadeNecessaria) {
        validarQuantidadePositiva(quantidadeNecessaria);
        if (!verificarDisponibilidade(quantidadeNecessaria)) {
            throw new IllegalStateException("Estoque insuficiente ou abaixo do mínimo de " + nome + ".");
        }
        quantidade -= quantidadeNecessaria;
    }

    // Separa o silício da tentativa sem perder sua origem.
    public PorcaoMateriaPrima retirarPorcao(double quantidade) {
        consumir(quantidade);
        return new PorcaoMateriaPrima(id, nome, unidade, quantidade);
    }

    public void adicionarEstoque(double quantidadeAdicional) {
        validarQuantidadePositiva(quantidadeAdicional);
        double novaQuantidade = quantidade + quantidadeAdicional;
        validarQuantidadeNaoNegativa(novaQuantidade);
        quantidade = novaQuantidade;
    }

    private void validarQuantidadeNaoNegativa(double valor) {
        if (!Double.isFinite(valor) || valor < 0.0) {
            throw new IllegalArgumentException("A quantidade deve ser finita e não negativa.");
        }
    }

    private void validarQuantidadePositiva(double valor) {
        validarQuantidadeNaoNegativa(valor);
        if (valor == 0.0) {
            throw new IllegalArgumentException("A quantidade movimentada deve ser positiva.");
        }
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public double getQuantidade() { return quantidade; }
    public String getUnidade() { return unidade; }
    public double getQuantidadeMinima() { return quantidadeMinima; }
    public double getCustoPorUnidade() { return custoPorUnidade; }
}
