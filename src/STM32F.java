// Modelo intermediário para controle e sinais.
public class STM32F extends Produto {

    public static final int CONSUMO_MATERIA_PRIMA = 5;

    public STM32F() {
        super("STM32F", CONSUMO_MATERIA_PRIMA, 0.7);
    }

    @Override
    public void processar() {
        configurarCircuito("Controle e processamento de sinais");
        setStatus(StatusProduto.PROCESSADO);
    }

    @Override
    public int calcularTempoProducao() {
        return 4;
    }

    @Override
    public String getTipo() {
        return "STM32F";
    }
}
