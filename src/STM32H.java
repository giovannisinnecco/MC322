// Modelo avançado para tarefas mais pesadas.
public class STM32H extends Produto {

    public static final int CONSUMO_MATERIA_PRIMA = 10;

    public STM32H() {
        super("STM32H", CONSUMO_MATERIA_PRIMA, 0.9);
    }

    @Override
    public void processar() {
        configurarCircuito("Processamento de maior complexidade");
        setStatus(StatusProduto.PROCESSADO);
    }

    @Override
    public int calcularTempoProducao() {
        return 6;
    }

    @Override
    public String getTipo() {
        return "STM32H";
    }
}
