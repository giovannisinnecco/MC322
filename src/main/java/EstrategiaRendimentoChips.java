import java.util.List;

/**
 * Escolhe o maior lote inteiro que cabe no orçamento, sem prever rejeições.
 * @author Giovanni Innecco e Rodrigo Soares
 */
public class EstrategiaRendimentoChips implements EstrategiaProducao {
    /** Cria a regra de seleção, sem guardar estado dos pedidos. */
    public EstrategiaRendimentoChips() { }

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
            if (elegivel(d) && d.ehViavel(orcamentoDisponivel)
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
    public String getNomeEstrategia() { return "Rendimento de chips (maior lote viável)"; }
}
