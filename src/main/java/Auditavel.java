/**
 * Permite consultar o diagnóstico e a necessidade de intervenção de máquinas e chips.
 * @author Giovanni Innecco e Rodrigo Soares
 */
public interface Auditavel {
    /**
     * Reúne as condições atuais do componente para a auditoria.
     * @return diagnóstico legível do componente
     */
    String gerarRelatorioDiagnostico();
    /**
     * Verifica se o componente precisa de intervenção.
     * @return true se o componente requer intervenção
     */
    boolean precisaManutencao();
}
