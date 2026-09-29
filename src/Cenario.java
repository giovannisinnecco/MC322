// Os valores são fictícios, só para a simulação.
public enum Cenario {
    IDEAL("Ideal", 10000, 1000, 0.1, 0.01, 0.3),
    APOCALIPTICO("Apocalíptico", 180, 50, 4.0, 0.40, 3.0);
    private final String nome;
    private final double budget, estoque, fatorFalha, riscoProduto, desgaste;
    Cenario(String nome, double budget, double estoque, double fatorFalha,
            double riscoProduto, double desgaste) {
        this.nome = nome; this.budget = budget; this.estoque = estoque;
        this.fatorFalha = fatorFalha; this.riscoProduto = riscoProduto; this.desgaste = desgaste;
    }
    public String getNome() { return nome; }
    public double getBudget() { return budget; }
    public double getEstoque() { return estoque; }
    public double getFatorFalha() { return fatorFalha; }
    public double getRiscoProduto() { return riscoProduto; }
    public double getDesgaste() { return desgaste; }
}
