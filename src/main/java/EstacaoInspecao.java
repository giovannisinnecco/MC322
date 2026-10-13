/**
 * Separa os chips aprovados e rejeitados. Uma falha da estação pode deixar um defeito passar.
 * @author Giovanni Innecco e Rodrigo Soares
 */
public class EstacaoInspecao extends Maquina {

    private int produtosInspecionados;

    /**
     * Cria a etapa com capacidade para dez unidades de silício.
     */
    public EstacaoInspecao() {
        this(10);
    }

    // A capacidade usa unidades de silício.
    /**
     * Cria a etapa com a capacidade de silício informada.
     * @param capacidade capacidade positiva em unidades de silício
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    public EstacaoInspecao(int capacidade) {
        super("Estação de Inspeção", capacidade, 0.05, 3.0);
        this.produtosInspecionados = 0;
    }

    /**
     * Executa a etapa de processamento deste componente.
     * @param produto chip recebido pela etapa
     * @throws EquipamentoIndisponivelException se o estado, a etapa ou a capacidade impedir a operação
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    @Override
    public void processar(Produto produto) {
        validarProcessamento(produto, StatusProduto.ENCAPSULADO);
        boolean rejeitado = verificarFalha(calcularChanceRejeicao(produto));
        boolean falhaInspecao = verificarFalha(getProbabilidadeFalha());
        if (falhaInspecao) {
            rejeitado = false;
        }
        produto.setStatus(rejeitado ? StatusProduto.REJEITADO : StatusProduto.APROVADO);
        produtosInspecionados++;
        registrarUso();
    }

    /**
     * Combina a exigência do modelo e o risco acumulado, limitado a um.
     * @param produto chip recebido pela etapa
     * @return chance de rejeição entre zero e um
     */
    private double calcularChanceRejeicao(Produto produto) {
        return Math.min(1.0, produto.getQualidade()
                * (0.10 + produto.getProbabilidadeFalhaAcumulada()));
    }

    /**
     * Consulta identificação do modelo ou da etapa.
     * @return identificação do modelo ou da etapa
     */
    @Override
    public String getTipo() {
        return "Inspeção";
    }

    /**
     * Restaura o contador quando a tentativa é desfeita.
     * @param totalAnterior contador salvo antes do ciclo
     */
    void restaurarTotalInspecionados(int totalAnterior) {
        produtosInspecionados = totalAnterior;
    }

    /**
     * Consulta inspeções confirmadas.
     * @return inspeções confirmadas
     */
    public int getTotalInspecionados() {
        return this.produtosInspecionados;
    }
}
