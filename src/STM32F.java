// Representa o modelo intermediário para controle e sinais.
public class STM32F extends Produto {

    // Define o consumo fixo deste modelo.
    public static final int CONSUMO_MATERIA_PRIMA = 5;

    // Cria o STM32F com consumo e qualidade próprios.
    public STM32F() {
        super("STM32F", CONSUMO_MATERIA_PRIMA, 0.7);
    }

    // Configura o circuito intermediário durante a fotolitografia.
    @Override
    public void processar() {
        configurarCircuito("Controle e processamento de sinais");
        setStatus("Processado");
    }

    // Retorna o tempo simulado deste modelo.
    @Override
    public int calcularTempoProducao() {
        return 4;
    }

    // Identifica o modelo no restante da fábrica.
    @Override
    public String getTipo() {
        return "STM32F";
    }
}
