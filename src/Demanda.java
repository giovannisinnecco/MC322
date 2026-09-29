// As estimativas não contam tentativas rejeitadas.
public class Demanda {
    private final String tipoProduto;
    private final int consumoPorUnidade;
    private final double custoPorUnidade;
    private int quantidadeProdutos;
    private StatusDemanda status;
    public Demanda(String tipoProduto, int quantidade) {
        this(tipoProduto, quantidade, consumo(tipoProduto), 18.0);
    }
    public Demanda(String tipoProduto, int quantidade, int consumo, double custo) {
        if (tipoProduto == null || tipoProduto.trim().isEmpty() || consumo <= 0
                || !Double.isFinite(custo) || custo <= 0) throw new IllegalArgumentException("Demanda inválida.");
        this.tipoProduto = tipoProduto; this.consumoPorUnidade = consumo; this.custoPorUnidade = custo;
        definirQuantidade(quantidade);
    }
    private static int consumo(String tipo) {
        if ("STM32G".equals(tipo)) return STM32G.CONSUMO_MATERIA_PRIMA;
        if ("STM32F".equals(tipo)) return STM32F.CONSUMO_MATERIA_PRIMA;
        if ("STM32H".equals(tipo)) return STM32H.CONSUMO_MATERIA_PRIMA;
        throw new IllegalArgumentException("Modelo desconhecido.");
    }
    // Atualizar permite reabrir um pedido cancelado.
    public void atualizarQuantidade(int quantidade) {
        if (quantidade < 0) throw new IllegalArgumentException("Quantidade negativa.");
        if (status == StatusDemanda.EM_PRODUCAO) throw new IllegalStateException("Demanda em produção.");
        definirQuantidade(quantidade);
    }
    private void definirQuantidade(int quantidade) {
        if (quantidade < 0) throw new IllegalArgumentException("Quantidade negativa.");
        quantidadeProdutos = quantidade;
        status = quantidade == 0 ? StatusDemanda.CONCLUIDA : StatusDemanda.PENDENTE;
    }
    public void iniciar() {
        if (status != StatusDemanda.PENDENTE) throw new IllegalStateException("Demanda não está pendente.");
        status = StatusDemanda.EM_PRODUCAO;
    }
    public void cancelar() {
        if (status != StatusDemanda.PENDENTE && status != StatusDemanda.EM_PRODUCAO)
            throw new IllegalStateException("Demanda já encerrada.");
        status = StatusDemanda.CANCELADA;
    }
    public void pausar() {
        if (status != StatusDemanda.EM_PRODUCAO) throw new IllegalStateException("Demanda não iniciada.");
        status = StatusDemanda.PENDENTE;
    }
    public void atender() {
        if (status != StatusDemanda.EM_PRODUCAO) throw new IllegalStateException("Demanda não está em produção.");
        if (--quantidadeProdutos == 0) status = StatusDemanda.CONCLUIDA;
    }
    public double calcularMateriaPrimaNecessaria(int consumo) {
        if (consumo <= 0) throw new IllegalArgumentException("Consumo inválido.");
        return (double) quantidadeProdutos * consumo;
    }
    public double calcularConsumoEstimado() { return calcularMateriaPrimaNecessaria(consumoPorUnidade); }
    public double calcularCustoEstimado() { return quantidadeProdutos * custoPorUnidade; }
    public boolean ehViavel(double budget) { return Double.isFinite(budget) && budget >= calcularCustoEstimado(); }
    public String getTipoProduto() { return tipoProduto; }
    public int getQuantidadeProdutos() { return quantidadeProdutos; }
    public StatusDemanda getStatus() { return status; }
    public boolean estaAtendida() { return status == StatusDemanda.CONCLUIDA; }
}
