/** Valida a carga antes de mover o mecanismo de uma etapa produtiva.
 * @author Giovanni Innecco e Rodrigo Soares
 */
final class AtuadorPrecisao implements Auditavel {
    private final String etapa;
    private final int resolucao;

    AtuadorPrecisao(String etapa, int resolucao) {
        this.etapa = etapa;
        this.resolucao = resolucao;
    }

    void validarCarga(double carga, int capacidade) {
        if (carga > capacidade || carga <= 0) {
            throw new EquipamentoIndisponivelException("Atuador de " + etapa + " não comporta a carga.");
        }
    }

    @Override
    public String gerarRelatorioDiagnostico() {
        return "Atuador de precisão " + etapa + ": resolução " + resolucao + " unidades";
    }

    @Override
    public boolean precisaManutencao() { return false; }
}
