import java.util.List;
/**
 * Escolhe a próxima demanda sem alterar pedidos, estoque ou orçamento.
 * @author Giovanni Innecco e Rodrigo Soares
 */
public interface EstrategiaProducao {
    /**
     * Escolhe um pedido pendente sem modificar a lista ou seus elementos.
     * @param demandas lista não nula de pedidos em ordem de chegada
     * @param orcamentoDisponivel orçamento disponível em reais
     * @return pedido escolhido, ou null se nenhum for elegível
     */
    Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel);
    /**
     * Consulta nome para o menu.
     * @return nome para o menu
     */
    String getNomeEstrategia();
    /**
     * Ignora referências nulas e pedidos que não estão pendentes.
     * @param demanda pedido consultado ou em produção
     * @return true apenas para pedidos pendentes
     */
    default boolean elegivel(Demanda demanda) {
        return demanda != null && demanda.getStatus() == StatusDemanda.PENDENTE;
    }
}
