/**
 * Representa o modelo básico para sensores e atuadores.
 * @author Giovanni Innecco e Rodrigo Soares
 */
public class STM32G extends Produto {

    /** Unidades de silício usadas por tentativa deste modelo. */
    public static final int CONSUMO_MATERIA_PRIMA = 3;

    /** Cria o modelo básico, com consumo 3 e qualidade exigida 0,5. */
    public STM32G() {
        super("STM32G", CONSUMO_MATERIA_PRIMA, 0.5);
    }

    /**
     * Executa a etapa de processamento deste componente.
     */
    @Override
    public void processar() {
        configurarCircuito("Controle básico de sensores e atuadores");
        setStatus(StatusProduto.PROCESSADO);
    }

    /**
     * Consulta tempo simulado de uma tentativa, sem espera real.
     * @return tempo simulado de uma tentativa, sem espera real
     */
    @Override
    public int calcularTempoProducao() {
        return 2;
    }

    /**
     * Consulta identificação do modelo ou da etapa.
     * @return identificação do modelo ou da etapa
     */
    @Override
    public String getTipo() {
        return "STM32G";
    }
}
