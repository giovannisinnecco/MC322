public class Produto {
    
     // Atributos
    private int id;
    private String nome;
    private String status;
    private double quantidadeMateriaPrimaNecessaria;

    // Construtor
    public Produto(int id, String nome, double quantidadeMateriaPrimaNecessaria) {
        this.id = id;
        this.nome = nome;
        this.status = "Aguardando";
        this.quantidadeMateriaPrimaNecessaria = quantidadeMateriaPrimaNecessaria;
    }

    // Métodos
    public void processar(){
        this.status = "Processado";
    }

    public void definirDemandaMateriaPrima(double quantidade) {
        this.quantidadeMateriaPrimaNecessaria = quantidade;
    }

    // Métodos de acesso (getters)
    public double getDemandaMateriaPrima() {
        return this.quantidadeMateriaPrimaNecessaria;
    }
    public int getId() {
        return this.id;
    }
    public String getNome() {
        return this.nome;
    }
    public String getStatus() {
        return this.status;
    }
}
