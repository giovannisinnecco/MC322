// Representa o modelo avançado para tarefas mais complexas.
public class STM32H extends Produto {

    // Define o consumo fixo deste modelo.
    public static final int CONSUMO_MATERIA_PRIMA = 10;

    // Cria o STM32H com consumo e qualidade próprios.
    public STM32H() {
        super("STM32H", CONSUMO_MATERIA_PRIMA, 0.9);
    }

    // Configura o circuito avançado durante a fotolitografia.
    @Override
    public void processar() {
        configurarCircuito("Processamento de maior complexidade");
        setStatus("Processado");
    }

    // Retorna o tempo simulado deste modelo.
    @Override
    public int calcularTempoProducao() {
        return 6;
    }

    // Identifica o modelo no restante da fábrica.
    @Override
    public String getTipo() {
        return "STM32H";
    }
}
