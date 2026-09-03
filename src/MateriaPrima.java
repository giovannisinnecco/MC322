public class MateriaPrima {

    // Atributos
    private int id;
    private String nome;
    private double quantidade;
    private String unidade;
    private double quantidadeMinima;

    // Construtor
    public MateriaPrima(int id, String nome, double quantidade, String unidade, double quantidadeMinima) {
        this.id = id;
        this.nome = nome;
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.quantidadeMinima = quantidadeMinima;
    }
    // Métodos
    public boolean verificarDisponibilidade(double quantidadeNecessaria) {
        return this.quantidade >= quantidadeNecessaria;
    }

    public void consumir(double quantidadeNecessaria) {
        if (verificarDisponibilidade(quantidadeNecessaria)) {
            this.quantidade -= quantidadeNecessaria;
        } else {
            System.out.println("Quantidade no estoque insuficiente. Disponível: " + this.quantidade + " " + this.unidade);
        }
    }

    public void adicionarEstoque(double quantidadeAdicional){
        if (quantidadeAdicional > 0) {
            this.quantidade += quantidadeAdicional;
        } 
    }

    // Metódos de acesso (getters)
    public int getId() {
        return this.id;
    }
    public String getNome() {
        return this.nome;
    }
    public double getQuantidade() {
        return this.quantidade;
    }
    public double getQuantidadeMinima() {
        return this.quantidadeMinima;
    }

}
