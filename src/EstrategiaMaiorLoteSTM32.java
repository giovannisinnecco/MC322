import java.util.List;

public class EstrategiaMaiorLoteSTM32 implements EstrategiaProducao {
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
    @Override public String getNomeEstrategia() { return "Maior lote STM32"; }
}
