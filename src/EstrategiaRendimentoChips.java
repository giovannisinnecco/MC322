import java.util.List;

public class EstrategiaRendimentoChips implements EstrategiaProducao {
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
    @Override public String getNomeEstrategia() { return "Rendimento de chips (maior lote viável)"; }
}
