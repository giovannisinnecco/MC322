// Acompanha quantas unidades aprovadas ainda faltam.
public class Demanda {

    // Guarda o modelo pedido e sua quantidade pendente.
    private final String tipoProduto;
    private int quantidadeProdutos;
    private boolean atendida;

    // Cria uma demanda para um modelo da fábrica.
    public Demanda(String tipoProduto, int quantidadeProdutos) {
        if (tipoProduto == null || tipoProduto.trim().isEmpty()) {
            throw new IllegalArgumentException("O tipo do produto deve ser informado.");
        }
        this.tipoProduto = tipoProduto.trim();
        definirQuantidade(quantidadeProdutos);
    }

    // Troca a quantidade pendente pelo novo valor.
    public void atualizarQuantidade(int novaQuantidade) {
        definirQuantidade(novaQuantidade);
    }

    // Valida a quantidade e atualiza o estado da demanda.
    private void definirQuantidade(int novaQuantidade) {
        if (novaQuantidade < 0) {
            throw new IllegalArgumentException("A quantidade pendente não pode ser negativa.");
        }
        quantidadeProdutos = novaQuantidade;
        atendida = quantidadeProdutos == 0;
    }

    // Calcula o material mínimo sem prever rejeições.
    public double calcularMateriaPrimaNecessaria(int quantidadeMateriaPrimaPorUnidade) {
        if (quantidadeMateriaPrimaPorUnidade <= 0) {
            throw new IllegalArgumentException("O consumo por unidade deve ser positivo.");
        }
        return (double) quantidadeProdutos * quantidadeMateriaPrimaPorUnidade;
    }

    // Registra uma unidade aprovada no pedido.
    public void atender() {
        if (atendida) {
            throw new IllegalStateException("Esta demanda não possui unidades pendentes.");
        }
        quantidadeProdutos--;
        atendida = quantidadeProdutos == 0;
    }

    // Libera a leitura do estado da demanda.
    public String getTipoProduto() { return tipoProduto; }
    public int getQuantidadeProdutos() { return quantidadeProdutos; }
    public boolean estaAtendida() { return atendida; }
}
