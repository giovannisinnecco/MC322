// Modelo básico para sensores e atuadores.
public class STM32G extends Produto {

    public static final int CONSUMO_MATERIA_PRIMA = 3;

    public STM32G() {
        super("STM32G", CONSUMO_MATERIA_PRIMA, 0.5);
    }

    @Override
    public void processar() {
        configurarCircuito("Controle básico de sensores e atuadores");
        setStatus(StatusProduto.PROCESSADO);
    }

    @Override
    public int calcularTempoProducao() {
        return 2;
    }

    @Override
    public String getTipo() {
        return "STM32G";
    }
}
