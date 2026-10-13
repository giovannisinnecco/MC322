/**
 * Protege o chip e pode acrescentar risco durante o encapsulamento.
 * @author Giovanni Innecco e Rodrigo Soares
 */
public class MaquinaEncapsulamento extends Maquina {

    /**
     * Cria a etapa com capacidade para dez unidades de silício.
     */
    public MaquinaEncapsulamento() {
        this(10);
    }

    // A capacidade usa unidades de silício.
    /**
     * Cria a etapa com a capacidade de silício informada.
     * @param capacidade capacidade positiva em unidades de silício
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    public MaquinaEncapsulamento(int capacidade) {
        super("Máquina de Encapsulamento", capacidade, 0.10, 5.0);
    }

    /**
     * Executa a etapa de processamento deste componente.
     * @param produto chip recebido pela etapa
     * @throws EquipamentoIndisponivelException se o estado, a etapa ou a capacidade impedir a operação
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    @Override
    public void processar(Produto produto) {
        validarProcessamento(produto, StatusProduto.PROCESSADO);
        if (verificarFalha(getProbabilidadeFalha())) {
            produto.aumentarProbabilidadeFalha(0.05);
        }
        produto.setStatus(StatusProduto.ENCAPSULADO);
        registrarUso();
    }

    /**
     * Consulta identificação do modelo ou da etapa.
     * @return identificação do modelo ou da etapa
     */
    @Override
    public String getTipo() {
        return "Encapsulamento";
    }
}
