/**
 * Guarda o saldo de aprovações de um pedido. As estimativas não incluem rejeições.
 * @author Giovanni Innecco e Rodrigo Soares
 */
public class Demanda {
    private final String tipoProduto;
    private final int consumoPorUnidade;
    private final double custoPorUnidade;
    private int quantidadeProdutos;
    private StatusDemanda status;
    /**
     * Abre um pedido; quantidade zero começa como concluída.
     * @param tipoProduto modelo solicitado
     * @param quantidade quantidade solicitada
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    public Demanda(String tipoProduto, int quantidade) {
        this(tipoProduto, quantidade, consumo(tipoProduto), 18.0);
    }
    /**
     * Abre um pedido; quantidade zero começa como concluída.
     * @param tipoProduto modelo solicitado
     * @param quantidade quantidade solicitada
     * @param consumo consumo positivo por unidade
     * @param custo custo operacional por tentativa
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    public Demanda(String tipoProduto, int quantidade, int consumo, double custo) {
        if (tipoProduto == null || tipoProduto.trim().isEmpty() || consumo <= 0
                || !Double.isFinite(custo) || custo <= 0) throw new IllegalArgumentException("Demanda inválida.");
        this.tipoProduto = tipoProduto; this.consumoPorUnidade = consumo; this.custoPorUnidade = custo;
        definirQuantidade(quantidade);
    }
    /**
     * Consulta o consumo do modelo no catálogo.
     * @param tipo modelo do catálogo
     * @return consumo de silício por tentativa do modelo
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    private static int consumo(String tipo) {
        if ("STM32G".equals(tipo)) return STM32G.CONSUMO_MATERIA_PRIMA;
        if ("STM32F".equals(tipo)) return STM32F.CONSUMO_MATERIA_PRIMA;
        if ("STM32H".equals(tipo)) return STM32H.CONSUMO_MATERIA_PRIMA;
        throw new IllegalArgumentException("Modelo desconhecido.");
    }
    // Atualizar permite reabrir um pedido cancelado.
    /**
     * Substitui o saldo e reabre o pedido; zero encerra a pendência.
     * @param quantidade quantidade solicitada
     * @throws DemandaInvalidaException se o modelo, o pedido ou sua transição não forem válidos
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    public void atualizarQuantidade(int quantidade) {
        if (quantidade < 0) throw new IllegalArgumentException("Quantidade negativa.");
        if (status == StatusDemanda.EM_PRODUCAO) throw new DemandaInvalidaException("Demanda em produção.");
        definirQuantidade(quantidade);
    }
    /**
     * Define o saldo inicial e o estado correspondente.
     * @param quantidade quantidade solicitada
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    private void definirQuantidade(int quantidade) {
        if (quantidade < 0) throw new IllegalArgumentException("Quantidade negativa.");
        quantidadeProdutos = quantidade;
        status = quantidade == 0 ? StatusDemanda.CONCLUIDA : StatusDemanda.PENDENTE;
    }
    /**
     * Coloca um pedido pendente em produção.
     * @throws DemandaInvalidaException se o modelo, o pedido ou sua transição não forem válidos
     */
    public void iniciar() {
        if (status != StatusDemanda.PENDENTE) throw new DemandaInvalidaException("Demanda não está pendente.");
        status = StatusDemanda.EM_PRODUCAO;
    }
    /**
     * Cancela o saldo sem apagar a quantidade ainda não atendida.
     * @throws DemandaInvalidaException se o modelo, o pedido ou sua transição não forem válidos
     */
    public void cancelar() {
        if (status != StatusDemanda.PENDENTE && status != StatusDemanda.EM_PRODUCAO)
            throw new DemandaInvalidaException("Demanda já encerrada.");
        status = StatusDemanda.CANCELADA;
    }
    /**
     * Devolve à fila um pedido interrompido durante a produção.
     * @throws DemandaInvalidaException se o modelo, o pedido ou sua transição não forem válidos
     */
    public void pausar() {
        if (status != StatusDemanda.EM_PRODUCAO) throw new DemandaInvalidaException("Demanda não iniciada.");
        status = StatusDemanda.PENDENTE;
    }
    /**
     * Desconta uma aprovação e conclui o pedido quando o saldo chega a zero.
     * @throws DemandaInvalidaException se o modelo, o pedido ou sua transição não forem válidos
     */
    public void atender() {
        if (status != StatusDemanda.EM_PRODUCAO) throw new DemandaInvalidaException("Demanda não está em produção.");
        if (--quantidadeProdutos == 0) status = StatusDemanda.CONCLUIDA;
    }
    /**
     * Estima o material do saldo, sem contar rejeições.
     * @param consumo consumo positivo por unidade
     * @return unidades de silício estimadas
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    public double calcularMateriaPrimaNecessaria(int consumo) {
        if (consumo <= 0) throw new IllegalArgumentException("Consumo inválido.");
        return (double) quantidadeProdutos * consumo;
    }
    /**
     * Estima o material do saldo usando o consumo do modelo.
     * @return unidades de silício estimadas
     */
    public double calcularConsumoEstimado() { return calcularMateriaPrimaNecessaria(consumoPorUnidade); }
    /**
     * Estima o custo do saldo, sem compra de material nem rejeições.
     * @return custo operacional estimado em reais
     */
    public double calcularCustoEstimado() { return quantidadeProdutos * custoPorUnidade; }
    /**
     * Confere se o orçamento paga o saldo inteiro estimado.
     * @param budget orçamento disponível em reais
     * @return true se todo o saldo cabe no orçamento
     */
    public boolean ehViavel(double budget) { return Double.isFinite(budget) && budget >= calcularCustoEstimado(); }
    /**
     * Consulta modelo solicitado.
     * @return modelo solicitado
     */
    public String getTipoProduto() { return tipoProduto; }
    /**
     * Consulta o saldo de aprovações ainda solicitado.
     * @return saldo de aprovações ainda solicitado
     */
    public int getQuantidadeProdutos() { return quantidadeProdutos; }
    /**
     * Consulta estado atual.
     * @return estado atual
     */
    public StatusDemanda getStatus() { return status; }
    /**
     * Consulta true se não há saldo pendente.
     * @return true se não há saldo pendente
     */
    public boolean estaAtendida() { return status == StatusDemanda.CONCLUIDA; }
}
