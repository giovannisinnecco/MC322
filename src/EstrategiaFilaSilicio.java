import java.util.List;

public class EstrategiaFilaSilicio implements EstrategiaProducao {
    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        for (Demanda d : demandas) {
            if (elegivel(d)) {
                return d;
            }
        }
        return null;
    }
    @Override public String getNomeEstrategia() { return "Fila de silício (ordem de chegada)"; }
}
