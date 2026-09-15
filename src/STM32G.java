// Representa o modelo básico voltado a sensores e atuadores.
public class STM32G extends Produto {

    // Define o consumo fixo deste modelo.
    public static final int CONSUMO_MATERIA_PRIMA = 3;

    // Cria o STM32G com consumo e qualidade próprios.
    public STM32G() {
        super("STM32G", CONSUMO_MATERIA_PRIMA, 0.5);
    }

    // Configura o circuito básico durante a fotolitografia.
    @Override
    public void processar() {
        configurarCircuito("Controle básico de sensores e atuadores");
        setStatus("Processado");
    }

    // Retorna o tempo simulado deste modelo.
    @Override
    public int calcularTempoProducao() {
        return 2;
    }

    // Identifica o modelo no restante da fábrica.
    @Override
    public String getTipo() {
        return "STM32G";
    }
}
