/** Controla a ativação e a sequência aceita por uma etapa da sala limpa.
 * @author Giovanni Innecco e Rodrigo Soares
 */
final class ControladorEtapa implements Auditavel {
    private final String etapa;
    private boolean ativo;

    ControladorEtapa(String etapa) {
        this.etapa = etapa;
    }

    void ativar() { ativo = true; }
    void desativar() { ativo = false; }

    void validar(StatusProduto esperado, StatusProduto recebido) {
        if (!ativo) throw new EquipamentoIndisponivelException("Controlador inativo na etapa " + etapa + ".");
        if (esperado != recebido) {
            throw new EquipamentoIndisponivelException("Sequência inválida na etapa " + etapa + ".");
        }
    }

    @Override
    public String gerarRelatorioDiagnostico() {
        return "Controlador " + etapa + ": " + (ativo ? "ativo" : "em espera");
    }

    @Override
    public boolean precisaManutencao() { return false; }
}
