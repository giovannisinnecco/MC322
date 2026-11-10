package br.unicamp.ic.mc322.model;

import br.unicamp.ic.mc322.exception.*;
/** Confere a precisão de alinhamento necessária para trabalhar o silício.
 * @author Giovanni Innecco e Rodrigo Soares
 */
final class SensorAlinhamento implements Auditavel {
    private final String etapa;
    private final double saudeMinima;

    SensorAlinhamento(String etapa, double saudeMinima) {
        this.etapa = etapa;
        this.saudeMinima = saudeMinima;
    }

    void validarLeitura(double saude) {
        if (saude < saudeMinima) {
            throw new EquipamentoIndisponivelException("Alinhamento insuficiente na etapa " + etapa + ".");
        }
    }

    @Override
    public String gerarRelatorioDiagnostico() {
        return "Sensor de alinhamento " + etapa + ": calibrado (limiar de saúde "
                + String.format("%.0f", saudeMinima) + ")";
    }

    @Override
    public boolean precisaManutencao() { return false; }
}
