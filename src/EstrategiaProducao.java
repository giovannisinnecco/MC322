import java.util.List;
public interface EstrategiaProducao {
    Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel);
    String getNomeEstrategia();
    default boolean elegivel(Demanda demanda) {
        return demanda != null && demanda.getStatus() == StatusDemanda.PENDENTE;
    }
}
