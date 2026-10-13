/**
 * Forma o circuito e pode acrescentar defeitos ao chip.
 * @author Giovanni Innecco e Rodrigo Soares
 */
public class MaquinaFotolitografia extends Maquina {

    /**
     * Cria a etapa com capacidade para dez unidades de silício.
     */
    public MaquinaFotolitografia() {
        this(10);
    }

    // A capacidade usa unidades de silício.
    /**
     * Cria a etapa com a capacidade de silício informada.
     * @param capacidade capacidade positiva em unidades de silício
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    public MaquinaFotolitografia(int capacidade) {
        super("Máquina de Fotolitografia", capacidade, 0.20, 10.0);
    }

    /**
     * Executa a etapa de processamento deste componente.
     * @param produto chip recebido pela etapa
     * @throws EquipamentoIndisponivelException se o estado, a etapa ou a capacidade impedir a operação
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    @Override
    public void processar(Produto produto) {
        validarProcessamento(produto, StatusProduto.AGUARDANDO);
        produto.processar();
        if (verificarFalha(getProbabilidadeFalha())) {
            produto.aumentarProbabilidadeFalha(0.10);
        }
        registrarUso();
    }

    /**
     * Consulta identificação do modelo ou da etapa.
     * @return identificação do modelo ou da etapa
     */
    @Override
    public String getTipo() {
        return "Fotolitografia";
    }
}
