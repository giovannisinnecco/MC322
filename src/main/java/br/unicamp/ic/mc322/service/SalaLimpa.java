package br.unicamp.ic.mc322.service;

import br.unicamp.ic.mc322.exception.*;
import br.unicamp.ic.mc322.model.*;
import br.unicamp.ic.mc322.strategy.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Agrupa as máquinas que formam a linha de produção de chips.
 * @author Giovanni Innecco e Rodrigo Soares
 */
final class SalaLimpa implements Auditavel {
    private final List<Maquina> etapas;

    SalaLimpa(List<Maquina> etapas) {
        if (etapas == null || etapas.isEmpty() || etapas.contains(null)) {
            throw new IllegalArgumentException("A sala limpa precisa de etapas válidas.");
        }
        this.etapas = new ArrayList<>(etapas);
    }

    List<Maquina> etapasInternas() { return Collections.unmodifiableList(etapas); }

    void configurarCenario(Cenario cenario) {
        for (Maquina etapa : etapas) etapa.configurarCenario(cenario);
    }

    void semearSorteios(long seed) {
        for (int i = 0; i < etapas.size(); i++) etapas.get(i).setSeed(seed + i);
    }

    double custoPorCiclo() {
        double custo = 0;
        for (Maquina etapa : etapas) custo += etapa.getCustoOperacao();
        return custo;
    }

    void ligarEtapas() { for (Maquina etapa : etapas) etapa.ligar(); }
    void desligarEtapas() { for (Maquina etapa : etapas) etapa.desligar(); }

    boolean algumaEtapaQuebrada() {
        for (Maquina etapa : etapas) if (etapa.getEstado() == EstadoMaquina.QUEBRADA) return true;
        return false;
    }

    boolean verificaCapacidade(int carga) {
        for (Maquina etapa : etapas) if (!etapa.verificarCapacidade(carga)) return false;
        return true;
    }

    int quantidadeEtapas() { return etapas.size(); }

    double custoDaEtapa(int indice) { return etapas.get(indice).getCustoOperacao(); }

    void processarEtapa(int indice, Produto produto) { etapas.get(indice).processar(produto); }

    double[] salvarSaudes() {
        double[] saudes = new double[etapas.size()];
        for (int i = 0; i < etapas.size(); i++) saudes[i] = etapas.get(i).getSaude();
        return saudes;
    }

    void restaurarSaudes(double[] saudes) {
        for (int i = 0; i < etapas.size(); i++) etapas.get(i).setSaude(saudes[i]);
    }

    int totalInspecionado() { return ((EstacaoInspecao) etapas.get(2)).getTotalInspecionados(); }

    void restaurarInspecoes(int totalAnterior) {
        ((EstacaoInspecao) etapas.get(2)).restaurarTotalInspecionados(totalAnterior);
    }

    @Override
    public String gerarRelatorioDiagnostico() {
        StringBuilder relatorio = new StringBuilder("Sala Limpa");
        for (Maquina etapa : etapas) {
            relatorio.append("\n  ").append(etapa.gerarRelatorioDiagnostico().replace("\n", "\n  "));
        }
        return relatorio.toString();
    }

    @Override
    public boolean precisaManutencao() {
        for (Maquina etapa : etapas) if (etapa.algumaPartePrecisaManutencao()) return true;
        return false;
    }
}
