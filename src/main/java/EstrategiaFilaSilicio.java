import java.util.List;

/**
 * Atende o primeiro pedido pendente na ordem da lista.
 * @author Giovanni Innecco e Rodrigo Soares
 */
public class EstrategiaFilaSilicio implements EstrategiaProducao {
    /** Cria a regra de seleção, sem guardar estado dos pedidos. */
    public EstrategiaFilaSilicio() { }

    /**
     * Escolhe um pedido pendente sem modificar a lista ou seus elementos.
     * @param demandas lista não nula de pedidos em ordem de chegada
     * @param orcamentoDisponivel orçamento disponível em reais
     * @return pedido escolhido, ou null se nenhum for elegível
     */
    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        for (Demanda d : demandas) {
            if (elegivel(d)) {
                return d;
            }
        }
        return null;
    }
    /**
     * Consulta nome para o menu.
     * @return nome para o menu
     */
    @Override
    public String getNomeEstrategia() { return "Fila de silício (ordem de chegada)"; }
}
