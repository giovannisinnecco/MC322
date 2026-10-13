/**
 * Representa o modelo intermediário para controle e sinais.
 * @author Giovanni Innecco e Rodrigo Soares
 */
public class STM32F extends Produto {

    /** Unidades de silício usadas por tentativa deste modelo. */
    public static final int CONSUMO_MATERIA_PRIMA = 5;

    /** Cria o modelo intermediário, com consumo 5 e qualidade exigida 0,7. */
    public STM32F() {
        super("STM32F", CONSUMO_MATERIA_PRIMA, 0.7);
    }

    /**
     * Executa a etapa de processamento deste componente.
     */
    @Override
    public void processar() {
        configurarCircuito("Controle e processamento de sinais");
        setStatus(StatusProduto.PROCESSADO);
    }

    /**
     * Consulta tempo simulado de uma tentativa, sem espera real.
     * @return tempo simulado de uma tentativa, sem espera real
     */
    @Override
    public int calcularTempoProducao() {
        return 4;
    }

    /**
     * Consulta identificação do modelo ou da etapa.
     * @return identificação do modelo ou da etapa
     */
    @Override
    public String getTipo() {
        return "STM32F";
    }
}
