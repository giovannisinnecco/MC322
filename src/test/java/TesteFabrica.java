import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Confere os fluxos e a recuperação da fábrica sem bibliotecas externas.
 * @author Giovanni Innecco e Rodrigo Soares
 */
public class TesteFabrica {
    private static int verificacoes;

    /** Não há estado de teste para instanciar. */
    private TesteFabrica() { }

    /**
     * Executa os casos e falha o build se alguma regra deixar de funcionar.
     * @param args argumentos não utilizados
     */
    public static void main(String[] args) {
        PrintStream saida = System.out;
        try (PrintStream silenciosa = new PrintStream(new ByteArrayOutputStream())) {
            System.setOut(silenciosa);
            conferirEstrategias();
            conferirRecursos();
            conferirEquipamentos();
            conferirFalhaNoCiclo();
            conferirProducao();
            conferirMenu();
        } finally {
            System.setOut(saida);
        }
        System.out.println("[TESTES] " + verificacoes + " verificações passaram.");
    }

    /** Confere seleção, estimativas e transições dos pedidos. */
    private static void conferirEstrategias() {
        Demanda pequena = new Demanda("STM32G", 2);
        Demanda grande = new Demanda("STM32F", 8);
        Demanda media = new Demanda("STM32H", 4);
        List<Demanda> fila = List.of(pequena, grande, media);
        conferir(new EstrategiaFilaSilicio().selecionarDemanda(fila, 100) == pequena, "ordem de chegada");
        conferir(new EstrategiaMaiorLoteSTM32().selecionarDemanda(fila, 100) == grande, "maior lote");
        conferir(new EstrategiaRendimentoChips().selecionarDemanda(fila, 100) == media, "lote viável");
        conferir(new EstrategiaRendimentoChips().selecionarDemanda(fila, 1) == null, "sem lote viável");
        conferir(pequena.calcularConsumoEstimado() == 6, "consumo estimado");
        pequena.iniciar();
        pequena.atender();
        conferir(pequena.getStatus() == StatusDemanda.EM_PRODUCAO, "aprovação parcial");
        pequena.atender();
        conferir(pequena.estaAtendida(), "conclusão");
        grande.cancelar();
        esperar(DemandaInvalidaException.class, grande::iniciar);
        esperar(DemandaInvalidaException.class, grande::atender);
        grande.atualizarQuantidade(3);
        conferir(grande.getStatus() == StatusDemanda.PENDENTE, "reabertura");
    }

    /** Confere que recusas não debitam recursos nem concluem pedidos. */
    private static void conferirRecursos() {
        MateriaPrima estoque = new MateriaPrima(1, "Silício", 100, "unidades", 10, 2);
        GerenciadorProducao g = new GerenciadorProducao(estoque, 17);
        esperar(DemandaInvalidaException.class, () -> g.fabricarDemanda("STM32G"));
        g.registrarDemanda("STM32G", 1);
        esperar(DemandaInvalidaException.class, () -> g.registrarDemanda("STM32G", 2));
        esperar(IllegalArgumentException.class, () -> g.atualizarDemanda("STM32G", -1));
        conferir(g.getQuantidadePendente("STM32G") == 1, "saldo após atualização inválida");
        esperar(RecursoInsuficienteException.class, () -> g.fabricarDemanda("STM32G"));
        conferir(estoque.getQuantidade() == 100 && g.getBudget() == 17, "produção recusada sem débito");
        conferir(g.getDemandas().get(0).getStatus() == StatusDemanda.CANCELADA, "cancelamento por orçamento");
        conferir(g.getTotalTentativas() == 0 && g.getTotalArmazenado() == 0, "nenhum chip criado na recusa");
        esperar(RecursoInsuficienteException.class, () -> g.comprarMateriaPrima(9));
        conferir(estoque.getQuantidade() == 100 && g.getBudget() == 17, "compra recusada sem débito");
        esperar(IllegalArgumentException.class, () -> g.comprarMateriaPrima(Double.NaN));
        g.comprarMateriaPrima(2.5);
        conferir(estoque.getQuantidade() == 102.5 && g.getBudget() == 12, "compra posterior ao erro");

        MateriaPrima pouco = new MateriaPrima(2, "Silício", 7, "unidades", 10, 2);
        GerenciadorProducao semMaterial = new GerenciadorProducao(pouco, 100);
        semMaterial.registrarDemanda("STM32G", 1);
        esperar(RecursoInsuficienteException.class, () -> semMaterial.fabricarDemanda("STM32G"));
        conferir(pouco.getQuantidade() == 7 && semMaterial.getBudget() == 100, "estoque mínimo preservado");
    }

    /** Confere capacidade, transporte, saúde e bloqueio de máquina quebrada. */
    private static void conferirEquipamentos() {
        MaquinaFotolitografia maquina = new MaquinaFotolitografia(2);
        esperar(EquipamentoIndisponivelException.class, () -> maquina.processar(new STM32G()));
        maquina.ligar();
        esperar(EquipamentoIndisponivelException.class, () -> maquina.processar(new STM32G()));
        double risco = maquina.getProbabilidadeFalha();
        maquina.setSaude(50);
        conferir(maquina.getProbabilidadeFalha() == risco * 2, "risco inverso à saúde");
        maquina.setSaude(29);
        conferir(maquina.precisaManutencao(), "limiar de manutenção");
        maquina.setSaude(0);
        maquina.desligar();
        esperar(EquipamentoIndisponivelException.class, maquina::ligar);
        conferir(maquina.getEstado() == EstadoMaquina.QUEBRADA, "quebra preservada");
        Esteira esteira = new Esteira();
        Produto chip = new STM32G();
        esperar(EquipamentoIndisponivelException.class, () -> esteira.adicionarItem(chip));
        esteira.ligar();
        esteira.adicionarItem(chip);
        esperar(EquipamentoIndisponivelException.class, () -> esteira.adicionarItem(new STM32G()));
        conferir(esteira.removerItem() == chip, "transporte após recusa");

        MateriaPrima estoque = new MateriaPrima(3, "Silício", 100, "unidades", 10, 2);
        GerenciadorProducao g = new GerenciadorProducao(estoque, 100,
                new MaquinaFotolitografia(2), new MaquinaEncapsulamento(), new EstacaoInspecao());
        g.registrarDemanda("STM32G", 1);
        esperar(EquipamentoIndisponivelException.class, () -> g.fabricarDemanda("STM32G"));
        conferir(estoque.getQuantidade() == 100 && g.getBudget() == 100, "capacidade validada antes do débito");
        conferir(g.getDemandas().get(0).getStatus() == StatusDemanda.PENDENTE, "capacidade mantém pedido");
    }

    /** Provoca erro depois da inspeção para conferir o desfazimento do ciclo. */
    private static void conferirFalhaNoCiclo() {
        MateriaPrima estoque = new MateriaPrima(4, "Silício", 100, "unidades", 10, 2);
        InspecaoComErro inspecao = new InspecaoComErro();
        GerenciadorProducao g = new GerenciadorProducao(estoque, 180,
                new MaquinaFotolitografia(), new MaquinaEncapsulamento(), inspecao);
        g.registrarDemanda("STM32G", 1);
        g.setSeed(123);
        RuntimeException falha = esperar(FalhaProducaoException.class, () -> g.fabricarDemanda("STM32G"));
        conferir(falha.getCause() instanceof IllegalStateException, "causa original preservada");
        conferir(estoque.getQuantidade() == 100 && g.getBudget() == 180, "restauração de recursos");
        conferir(inspecao.getTotalInspecionados() == 0, "restauração da contagem de inspeções");
        conferir(g.getTotalTentativas() == 0 && g.getTempoTotalProducao() == 0, "tentativa não confirmada");
        conferir(g.getQuantidadePendente("STM32G") == 1 && g.getTotalArmazenado() == 0, "pedido não concluído");
        for (Maquina maquina : g.getMaquinas()) {
            conferir(maquina.getSaude() == 100 && !maquina.estaLigada(), "saúde restaurada e linha desligada");
        }
        inspecao.falhar = false;
        g.fabricarDemanda("STM32G");
        conferir(g.getTotalArmazenado() == 1, "recuperação após falha inesperada");
        conferir(estoque.getQuantidade() == 100 - 3 * g.getTotalTentativas(), "estoque após recuperação");

        // Uma falha posterior não pode apagar o primeiro chip aprovado.
        InspecaoComErro segundaInspecao = new InspecaoComErro();
        segundaInspecao.falharApos = 2;
        MateriaPrima outroEstoque = new MateriaPrima(5, "Silício", 100, "unidades", 10, 2);
        GerenciadorProducao parcial = new GerenciadorProducao(outroEstoque, 180,
                new MaquinaFotolitografia(), new MaquinaEncapsulamento(), segundaInspecao);
        parcial.setSeed(123);
        parcial.registrarDemanda("STM32G", 2);
        esperar(FalhaProducaoException.class, () -> parcial.fabricarDemanda("STM32G"));
        conferir(parcial.getTotalTentativas() == 1 && segundaInspecao.getTotalInspecionados() == 1,
                "falha posterior preserva contadores anteriores");
        conferir(outroEstoque.getQuantidade() == 97 && parcial.getBudget() == 162,
                "falha posterior desfaz apenas o segundo débito");
        conferir(parcial.getTotalArmazenado() == 1 && parcial.getQuantidadePendente("STM32G") == 1,
                "falha posterior preserva a primeira aprovação");
    }

    /** Confere o fluxo antigo, cenários, armazém e interrupção parcial. */
    private static void conferirProducao() {
        GerenciadorProducao g = new GerenciadorProducao(Cenario.IDEAL, new EstrategiaFilaSilicio());
        g.setSeed(123);
        g.registrarDemanda("STM32G", 0);
        g.registrarDemanda("STM32H", 0);
        g.atualizarDemanda("STM32H", 2);
        g.atualizarDemanda("STM32G", 1);
        g.executarProximaProducao();
        conferir(g.getQuantidadeArmazenada("STM32H") == 2, "ordem de chegada real");
        g.setEstrategia(new EstrategiaMaiorLoteSTM32());
        g.executarProximaProducao();
        conferir(g.getTotalArmazenado() == 3, "troca dinâmica");
        conferir(g.getBudget() == 10000 - 18 * g.getTotalTentativas(), "custo de todas as tentativas");
        for (Produto chip : g.getProdutosFabricados()) {
            conferir(chip.getOrigem() != null && chip.getLote() > 0
                    && chip.getStatus() == StatusProduto.APROVADO, "armazém rastreável");
        }
        g.gerarAuditoriaGeral();
        g.exibirArmazem();

        GerenciadorProducao ap = new GerenciadorProducao(Cenario.APOCALIPTICO, new EstrategiaFilaSilicio());
        ap.setSeed(123);
        ap.registrarDemanda("STM32H", 100);
        esperar(RecursoInsuficienteException.class, ap::executarProximaProducao);
        conferir(ap.getBudget() == 90 && ap.getTotalTentativas() == 5, "ciclos anteriores preservados");
        conferir(ap.getQuantidadePendente("STM32H") == 100 - ap.getTotalArmazenado(), "saldo parcial coerente");
        conferir(ap.getDemandas().get(0).getStatus() == StatusDemanda.CANCELADA, "saldo cancelado");
        ap.comprarMateriaPrima(5);
        esperar(DemandaInvalidaException.class, () -> ap.atualizarDemanda("STM32G", 0));
    }

    /** Confere que o terminal continua após erros e aceita fim de entrada. */
    private static void conferirMenu() {
        InputStream entrada = System.in;
        PrintStream saida = System.out;
        ByteArrayOutputStream texto = new ByteArrayOutputStream();
        String roteiro = "abc\n9\n1\n2\n2\n4\n999999\n3\n2\n0\n0\n";
        try (PrintStream captura = new PrintStream(texto, true, StandardCharsets.UTF_8)) {
            System.setIn(new ByteArrayInputStream(roteiro.getBytes(StandardCharsets.UTF_8)));
            System.setOut(captura);
            Main.main(new String[0]);
            String resultado = texto.toString(StandardCharsets.UTF_8);
            conferir(resultado.contains("[ERRO]"), "entrada inválida no menu");
            conferir(resultado.contains("[FÁBRICA]"), "erro de domínio no menu");
            conferir(resultado.contains("Preço de compra"), "consulta depois dos erros");
            conferir(resultado.contains("Turno encerrado"), "saída normal");
            texto.reset();
            System.setIn(new ByteArrayInputStream(new byte[0]));
            Main.main(new String[0]);
            conferir(texto.toString(StandardCharsets.UTF_8).contains("Entrada encerrada"), "fim da entrada");
        } finally {
            System.setIn(entrada);
            System.setOut(saida);
        }
    }

    /**
     * Registra uma condição e interrompe o teste se ela não valer.
     * @param condicao resultado esperado
     * @param caso descrição da regra
     */
    private static void conferir(boolean condicao, String caso) {
        verificacoes++;
        if (!condicao) throw new AssertionError(caso);
    }

    /**
     * Confere o tipo da exceção, sem aceitar falhas diferentes.
     * @param tipo exceção esperada
     * @param operacao operação que deve ser recusada
     * @return exceção capturada para conferir sua causa quando necessário
     */
    private static RuntimeException esperar(Class<? extends RuntimeException> tipo, Runnable operacao) {
        try {
            operacao.run();
        } catch (RuntimeException erro) {
            conferir(tipo.isInstance(erro), "exceção esperada: " + tipo.getSimpleName());
            return erro;
        }
        throw new AssertionError("Não lançou " + tipo.getSimpleName());
    }

    /** Simula um defeito de implementação após uma inspeção real. */
    private static class InspecaoComErro extends EstacaoInspecao {
        private boolean falhar = true;
        private int falharApos = 1;

        /**
         * Inspeciona e, se configurado, interrompe a tentativa.
         * @param produto chip recebido
         * @throws IllegalStateException quando a falha está habilitada
         */
        @Override
        public void processar(Produto produto) {
            super.processar(produto);
            if (falhar && getTotalInspecionados() >= falharApos)
                throw new IllegalStateException("Falha de teste após a inspeção.");
        }
    }
}
