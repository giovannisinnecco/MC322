// Guarda de onde veio o silício separado para um chip.
public final class PorcaoMateriaPrima {
    private final int idMateriaPrima;
    private final String nome, unidade;
    private final double quantidade;

    PorcaoMateriaPrima(int id, String nome, String unidade, double quantidade) {
        if (id <= 0 || nome == null || nome.trim().isEmpty() || unidade == null
                || unidade.trim().isEmpty() || !Double.isFinite(quantidade) || quantidade <= 0)
            throw new IllegalArgumentException("Porção inválida.");
        this.idMateriaPrima = id;
        this.nome = nome;
        this.unidade = unidade;
        this.quantidade = quantidade;
    }
    public int getIdMateriaPrima() { return idMateriaPrima; }
    public String getNome() { return nome; }
    public String getUnidade() { return unidade; }
    public double getQuantidade() { return quantidade; }
    public String descrever() {
        return "insumo #" + idMateriaPrima + " " + nome + ": " + quantidade + " " + unidade;
    }
}
