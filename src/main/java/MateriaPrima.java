/**
 * Guarda o silício, seu preço e o estoque mínimo para começar um ciclo.
 * @author Giovanni Innecco e Rodrigo Soares
 */
public class MateriaPrima {

    private final int id;
    private final String nome;
    private double quantidade;
    private final String unidade;
    private final double quantidadeMinima;
    private final double custoPorUnidade;

    /**
     * Abre um estoque com identidade, mínimo e preço de compra.
     * @param id identificador positivo do insumo
     * @param nome nome definido no código
     * @param quantidade quantidade solicitada
     * @param unidade unidade de medida não vazia
     * @param quantidadeMinima estoque mínimo finito e não negativo
     * @param custoPorUnidade preço finito e positivo em reais
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    public MateriaPrima(int id, String nome, double quantidade, String unidade,
                        double quantidadeMinima, double custoPorUnidade) {
        if (id <= 0) {
            throw new IllegalArgumentException("O ID da matéria-prima deve ser positivo.");
        }
        if (nome == null || nome.trim().isEmpty() || unidade == null || unidade.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome e unidade devem ser informados.");
        }
        validarQuantidadeNaoNegativa(quantidade);
        validarQuantidadeNaoNegativa(quantidadeMinima);
        if (!Double.isFinite(custoPorUnidade) || custoPorUnidade <= 0.0) {
            throw new IllegalArgumentException("O custo por unidade deve ser finito e positivo.");
        }
        this.id = id;
        this.nome = nome;
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.quantidadeMinima = quantidadeMinima;
        this.custoPorUnidade = custoPorUnidade;
    }

    /**
     * Confere consumo e mínimo antes da tentativa; consulta zero não exige o mínimo.
     * @param quantidadeNecessaria consumo solicitado em unidades de silício
     * @return true se o estoque permite o consumo
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    public boolean verificarDisponibilidade(double quantidadeNecessaria) {
        validarQuantidadeNaoNegativa(quantidadeNecessaria);
        // O mínimo precisa existir antes de começar o ciclo.
        return quantidade >= quantidadeNecessaria
                && (quantidadeNecessaria == 0 || quantidade >= quantidadeMinima);
    }

    /**
     * Debita o silício somente se houver quantidade e estoque mínimo.
     * @param quantidadeNecessaria consumo solicitado em unidades de silício
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     * @throws RecursoInsuficienteException se faltar silício ou orçamento para a operação
     */
    public void consumir(double quantidadeNecessaria) {
        validarQuantidadePositiva(quantidadeNecessaria);
        if (!verificarDisponibilidade(quantidadeNecessaria)) {
            throw new RecursoInsuficienteException("Estoque insuficiente ou abaixo do mínimo de " + nome + ".");
        }
        quantidade -= quantidadeNecessaria;
    }

    // Separa o silício da tentativa sem perder sua origem.
    /**
     * Debita o estoque e cria um registro imutável do material separado.
     * @param quantidade quantidade solicitada
     * @return registro do silício debitado
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     * @throws RecursoInsuficienteException se faltar silício ou orçamento para a operação
     */
    public PorcaoMateriaPrima retirarPorcao(double quantidade) {
        consumir(quantidade);
        return new PorcaoMateriaPrima(id, nome, unidade, quantidade);
    }

    /**
     * Soma uma quantidade positiva sem permitir estoque infinito.
     * @param quantidadeAdicional quantidade a acrescentar
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    public void adicionarEstoque(double quantidadeAdicional) {
        validarQuantidadePositiva(quantidadeAdicional);
        double novaQuantidade = quantidade + quantidadeAdicional;
        validarQuantidadeNaoNegativa(novaQuantidade);
        quantidade = novaQuantidade;
    }

    /**
     * Rejeita quantidades negativas ou não finitas.
     * @param valor valor a validar ou aplicar
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    private void validarQuantidadeNaoNegativa(double valor) {
        if (!Double.isFinite(valor) || valor < 0.0) {
            throw new IllegalArgumentException("A quantidade deve ser finita e não negativa.");
        }
    }

    /**
     * Rejeita quantidades nulas, negativas ou não finitas.
     * @param valor valor a validar ou aplicar
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    private void validarQuantidadePositiva(double valor) {
        validarQuantidadeNaoNegativa(valor);
        if (valor == 0.0) {
            throw new IllegalArgumentException("A quantidade movimentada deve ser positiva.");
        }
    }

    // Só o gerenciador usa a reposição exata ao desfazer um ciclo.
    /**
     * Repõe exatamente o estoque anterior ao desfazer uma tentativa.
     * @param quantidadeAnterior estoque salvo antes do ciclo
     * @throws IllegalArgumentException se a quantidade não for finita ou for negativa
     */
    void restaurarQuantidade(double quantidadeAnterior) {
        validarQuantidadeNaoNegativa(quantidadeAnterior);
        quantidade = quantidadeAnterior;
    }

    /**
     * Consulta identificador do objeto.
     * @return identificador do objeto
     */
    public int getId() { return id; }
    /**
     * Consulta nome definido no código.
     * @return nome definido no código
     */
    public String getNome() { return nome; }
    /**
     * Consulta quantidade de silício em unidades.
     * @return quantidade de silício em unidades
     */
    public double getQuantidade() { return quantidade; }
    /**
     * Consulta unidade de medida.
     * @return unidade de medida
     */
    public String getUnidade() { return unidade; }
    /**
     * Consulta mínimo exigido antes de cada tentativa.
     * @return mínimo exigido antes de cada tentativa
     */
    public double getQuantidadeMinima() { return quantidadeMinima; }
    /**
     * Consulta preço de compra de uma unidade de silício.
     * @return preço de compra de uma unidade de silício
     */
    public double getCustoPorUnidade() { return custoPorUnidade; }
}
