/**
 * Reúne os valores fictícios de orçamento, risco e desgaste do turno.
 * @author Giovanni Innecco e Rodrigo Soares
 */
public enum Cenario {
    /** Recursos fartos, baixo risco e pouco desgaste. */
    IDEAL("Ideal", 10000, 1000, 0.1, 0.01, 0.3),
    /** Recursos escassos, risco alto e desgaste acelerado. */
    APOCALIPTICO("Apocalíptico", 180, 50, 4.0, 0.40, 3.0);
    private final String nome;
    private final double budget, estoque, fatorFalha, riscoProduto, desgaste;
    /**
     * Reúne os parâmetros usados para abrir a fábrica.
     * @param nome nome definido no código
     * @param budget orçamento disponível em reais
     * @param estoque silício inicial
     * @param fatorFalha multiplicador do risco
     * @param riscoProduto risco inicial dos chips
     * @param desgaste desgaste máximo por uso
     */
    Cenario(String nome, double budget, double estoque, double fatorFalha,
            double riscoProduto, double desgaste) {
        this.nome = nome; this.budget = budget; this.estoque = estoque;
        this.fatorFalha = fatorFalha; this.riscoProduto = riscoProduto; this.desgaste = desgaste;
    }
    /**
     * Consulta nome definido no código.
     * @return nome definido no código
     */
    public String getNome() { return nome; }
    /**
     * Consulta orçamento em reais.
     * @return orçamento em reais
     */
    public double getBudget() { return budget; }
    /**
     * Consulta estoque inicial de silício.
     * @return estoque inicial de silício
     */
    public double getEstoque() { return estoque; }
    /**
     * Consulta multiplicador do risco das máquinas.
     * @return multiplicador do risco das máquinas
     */
    public double getFatorFalha() { return fatorFalha; }
    /**
     * Consulta risco inicial de cada chip.
     * @return risco inicial de cada chip
     */
    public double getRiscoProduto() { return riscoProduto; }
    /**
     * Consulta limite superior do desgaste aleatório por uso.
     * @return limite superior do desgaste aleatório por uso
     */
    public double getDesgaste() { return desgaste; }
}
