/**
 * Guarda a origem e a quantidade do silício separado para um chip.
 * @author Giovanni Innecco e Rodrigo Soares
 */
public final class PorcaoMateriaPrima {
    private final int idMateriaPrima;
    private final String nome, unidade;
    private final double quantidade;

    /**
     * Registra uma quantidade positiva retirada do estoque de origem.
     * @param id identificador positivo do insumo
     * @param nome nome definido no código
     * @param unidade unidade de medida não vazia
     * @param quantidade quantidade solicitada
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    PorcaoMateriaPrima(int id, String nome, String unidade, double quantidade) {
        if (id <= 0 || nome == null || nome.trim().isEmpty() || unidade == null
                || unidade.trim().isEmpty() || !Double.isFinite(quantidade) || quantidade <= 0)
            throw new IllegalArgumentException("Porção inválida.");
        this.idMateriaPrima = id;
        this.nome = nome;
        this.unidade = unidade;
        this.quantidade = quantidade;
    }
    /**
     * Consulta identificador do estoque de origem.
     * @return identificador do estoque de origem
     */
    public int getIdMateriaPrima() { return idMateriaPrima; }
    /**
     * Consulta nome definido no código.
     * @return nome definido no código
     */
    public String getNome() { return nome; }
    /**
     * Consulta unidade de medida.
     * @return unidade de medida
     */
    public String getUnidade() { return unidade; }
    /**
     * Consulta quantidade de silício em unidades.
     * @return quantidade de silício em unidades
     */
    public double getQuantidade() { return quantidade; }
    /**
     * Formata a identificação e a quantidade da origem do chip.
     * @return origem e quantidade em uma linha
     */
    public String descrever() {
        return "insumo #" + idMateriaPrima + " " + nome + ": " + quantidade + " " + unidade;
    }
}
