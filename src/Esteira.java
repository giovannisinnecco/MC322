import java.util.ArrayList;

// Transporta os produtos pela linha em ordem de chegada.
public class Esteira {

    // Guarda a fila e o estado atual da esteira.
    private final ArrayList<Produto> produtos = new ArrayList<>();
    private boolean emMovimento;
    private final int capacidadeMaxima;

    // Cria uma esteira para uma unidade por vez.
    public Esteira() {
        this(1);
    }

    // Cria uma esteira com a capacidade escolhida.
    public Esteira(int capacidadeMaxima) {
        if (capacidadeMaxima <= 0) {
            throw new IllegalArgumentException("A capacidade da esteira deve ser positiva.");
        }
        this.capacidadeMaxima = capacidadeMaxima;
        this.emMovimento = false;
    }

    // Controla o movimento e expõe o estado da fila.
    public void ligar() { emMovimento = true; }
    public void desligar() { emMovimento = false; }
    public boolean estaEmMovimento() { return emMovimento; }
    public int getCapacidadeMaxima() { return capacidadeMaxima; }
    public int getQuantidadeProdutos() { return produtos.size(); }

    // Confere se ainda há espaço na fila.
    public boolean verificarCapacidade(int quantidadeAdicional) {
        if (quantidadeAdicional < 0) {
            throw new IllegalArgumentException("A quantidade adicional não pode ser negativa.");
        }
        return quantidadeAdicional <= capacidadeMaxima - produtos.size();
    }

    // Coloca um produto no fim da fila.
    public void adicionarItem(Produto produto) {
        verificarMovimento();
        if (produto == null) {
            throw new IllegalArgumentException("O produto deve ser informado.");
        }
        if (produtos.contains(produto)) {
            throw new IllegalStateException("Esse produto já está na esteira.");
        }
        if (!verificarCapacidade(1)) {
            throw new IllegalStateException("A esteira está cheia.");
        }
        produtos.add(produto);
    }

    // Retira o produto que está há mais tempo na fila.
    public Produto removerItem() {
        verificarMovimento();
        if (produtos.isEmpty()) {
            throw new IllegalStateException("A esteira está vazia.");
        }
        return produtos.remove(0);
    }

    // Impede operações enquanto a esteira está parada.
    private void verificarMovimento() {
        if (!emMovimento) {
            throw new IllegalStateException("A esteira está desligada.");
        }
    }
}
