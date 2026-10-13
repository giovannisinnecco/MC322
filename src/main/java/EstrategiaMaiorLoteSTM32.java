import java.util.List;

/**
 * Prioriza o maior saldo pendente, sem filtrar o orçamento.
 * @author Giovanni Innecco e Rodrigo Soares
 */
public class EstrategiaMaiorLoteSTM32 implements EstrategiaProducao {
    /** Cria a regra de seleção, sem guardar estado dos pedidos. */
    public EstrategiaMaiorLoteSTM32() { }

    /**
     * Escolhe um pedido pendente sem modificar a lista ou seus elementos.
     * @param demandas lista não nula de pedidos em ordem de chegada
     * @param orcamentoDisponivel orçamento disponível em reais
     * @return pedido escolhido, ou null se nenhum for elegível
     */
    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        Demanda melhor = null;
        for (Demanda d : demandas) {
            if (elegivel(d)
                    && (melhor == null || d.getQuantidadeProdutos() > melhor.getQuantidadeProdutos())) {
                melhor = d;
            }
        }
        return melhor;
    }
    /**
     * Consulta nome para o menu.
     * @return nome para o menu
     */
    @Override
    public String getNomeEstrategia() { return "Maior lote STM32"; }
}
