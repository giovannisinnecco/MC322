package br.unicamp.ic.mc322;

import br.unicamp.ic.mc322.exception.*;
import br.unicamp.ic.mc322.model.*;
import br.unicamp.ic.mc322.service.*;
import br.unicamp.ic.mc322.strategy.*;
import br.unicamp.ic.mc322.persistence.PersistenciaPlanta;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Testes automatizados do armazenamento e da recuperação do estado da fábrica. */
class PersistenciaPlantaTest {
    private Path pastaTemporaria;

    @BeforeEach
    void criarPastaTemporaria() throws IOException {
        Path base = Path.of("build", "tmp", "dados-teste");
        Files.createDirectories(base);
        pastaTemporaria = Files.createTempDirectory(base, "planta-");
    }

    @AfterEach
    void removerPastaTemporaria() throws IOException {
        if (pastaTemporaria != null && Files.exists(pastaTemporaria)) {
            try (var caminhos = Files.walk(pastaTemporaria)) {
                for (Path caminho : caminhos.sorted(Comparator.reverseOrder()).toList()) {
                    Files.deleteIfExists(caminho);
                }
            }
        }
    }

    @Test
    void salvaERecuperaEstoqueMaquinasEEstadosDeDemandas() {
        Path arquivo = pastaTemporaria.resolve("planta.xml");
        GerenciadorProducao original = new GerenciadorProducao(Cenario.IDEAL, new EstrategiaRendimentoChips());
        original.comprarMateriaPrima(12.5);
        original.getMaquinas().get(0).setSaude(82.5);
        original.getMaquinas().get(0).setLimiteManutencao(45);
        original.getMaquinas().get(0).ligar();
        original.getMaquinas().get(1).setSaude(0);
        original.registrarDemanda("STM32G", 4);
        original.registrarDemanda("STM32F", 0);
        original.getDemandas().get(0).cancelar();

        PersistenciaPlanta persistencia = new PersistenciaPlanta(arquivo);
        persistencia.salvar(original);
        GerenciadorProducao recuperada = persistencia.carregar();

        assertEquals(original.getMateriaPrima().getQuantidade(), recuperada.getMateriaPrima().getQuantidade(), 0.000001);
        assertEquals(original.getBudget(), recuperada.getBudget(), 0.000001);
        assertEquals(82.5, recuperada.getMaquinas().get(0).getSaude(), 0.000001);
        assertEquals(original.getMaquinas().get(0).getCapacidadeMaxima(),
                recuperada.getMaquinas().get(0).getCapacidadeMaxima());
        assertEquals(45, recuperada.getMaquinas().get(0).getLimiteManutencao(), 0.000001);
        assertTrue(recuperada.getMaquinas().get(0).estaLigada());
        assertEquals(EstadoMaquina.QUEBRADA, recuperada.getMaquinas().get(1).getEstado());
        assertEquals(StatusDemanda.CANCELADA, recuperada.getDemandas().get(0).getStatus());
        assertEquals(StatusDemanda.CONCLUIDA, recuperada.getDemandas().get(1).getStatus());
        assertEquals("Rendimento de chips (maior lote viável)", recuperada.getNomeEstrategia());
    }

    @Test
    void rejeitaArquivoAusenteOuCorrompidoSemConstruirFabricaParcial() throws Exception {
        Path ausente = pastaTemporaria.resolve("ausente.xml");
        assertThrows(PersistenciaException.class, () -> new PersistenciaPlanta(ausente).carregar());
        Path invalido = pastaTemporaria.resolve("invalido.xml");
        Files.writeString(invalido, "<planta><dados>");
        assertThrows(PersistenciaException.class, () -> new PersistenciaPlanta(invalido).carregar());
    }

    @Test
    void faltaDeEstoqueCancelaDemandaETransicaoIlegalProduzExcecaoEspecifica() {
        GerenciadorProducao fabrica = new GerenciadorProducao(
                new MateriaPrima(1, "Silício", 0, "unidades", 0, 2), 100);
        fabrica.registrarDemanda("STM32G", 1);

        assertThrows(RecursoInsuficienteException.class, () -> fabrica.fabricarDemanda("STM32G"));
        assertEquals(StatusDemanda.CANCELADA, fabrica.getDemandas().get(0).getStatus());
        Demanda demanda = new Demanda("STM32F", 1);
        demanda.cancelar();
        assertThrows(DemandaInvalidaException.class, demanda::iniciar);
    }

    @Test
    void suiteLegadaContinuaVerificandoRegrasDaFabrica() {
        assertDoesNotThrow(() -> VerificadorFabrica.main(new String[0]));
    }
}
