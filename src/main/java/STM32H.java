/**
 * Representa o modelo avançado para processamento mais pesado.
 * @author Giovanni Innecco e Rodrigo Soares
 */
public class STM32H extends Produto {

    /** Unidades de silício usadas por tentativa deste modelo. */
    public static final int CONSUMO_MATERIA_PRIMA = 10;

    /** Cria o modelo avançado, com consumo 10 e qualidade exigida 0,9. */
    public STM32H() {
        super("STM32H", CONSUMO_MATERIA_PRIMA, 0.9);
    }

    /**
     * Executa a etapa de processamento deste componente.
     */
    @Override
    public void processar() {
        configurarCircuito("Processamento de maior complexidade");
        setStatus(StatusProduto.PROCESSADO);
    }

    /**
     * Consulta tempo simulado de uma tentativa, sem espera real.
     * @return tempo simulado de uma tentativa, sem espera real
     */
    @Override
    public int calcularTempoProducao() {
        return 6;
    }

    /**
     * Consulta identificação do modelo ou da etapa.
     * @return identificação do modelo ou da etapa
     */
    @Override
    public String getTipo() {
        return "STM32H";
    }
}
