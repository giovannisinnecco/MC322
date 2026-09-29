import java.util.ArrayList;

// Leva o silício e os chips de uma etapa para a outra.
public class Esteira {

    private final ArrayList<Produto> produtos = new ArrayList<>();
    private boolean emMovimento;
    private final int capacidadeMaxima;
    private final double capacidadeSilicio;
    private PorcaoMateriaPrima porcao;

    public Esteira() {
        this(1);
    }

    public Esteira(int capacidadeMaxima) {
        this(capacidadeMaxima, 10);
    }

    public Esteira(int capacidadeMaxima, double capacidadeSilicio) {
        if (capacidadeMaxima <= 0) {
            throw new IllegalArgumentException("A capacidade da esteira deve ser positiva.");
        }
        if (!Double.isFinite(capacidadeSilicio) || capacidadeSilicio <= 0)
            throw new IllegalArgumentException("Capacidade de silício inválida.");
        this.capacidadeSilicio = capacidadeSilicio;
        this.capacidadeMaxima = capacidadeMaxima;
        this.emMovimento = false;
    }

    public void ligar() { emMovimento = true; }
    public void desligar() { emMovimento = false; }
    public boolean estaEmMovimento() { return emMovimento; }
    public int getCapacidadeMaxima() { return capacidadeMaxima; }
    public int getQuantidadeProdutos() { return produtos.size(); }

    public boolean verificarCapacidade(int quantidadeAdicional) {
        if (quantidadeAdicional < 0) {
            throw new IllegalArgumentException("A quantidade adicional não pode ser negativa.");
        }
        return quantidadeAdicional <= capacidadeMaxima - produtos.size() - (porcao == null ? 0 : 1);
    }

    public void adicionarItem(Produto produto) {
        verificarMovimento();
        if (produto == null) {
            throw new IllegalArgumentException("O produto deve ser informado.");
        }
        if (porcao != null) throw new IllegalStateException("Esteira transportando matéria-prima.");
        if (produtos.contains(produto)) {
            throw new IllegalStateException("Esse produto já está na esteira.");
        }
        if (!verificarCapacidade(1) || !verificarCarga(produto.getQuantidadeMateriaPrimaPorUnidade())) {
            throw new IllegalStateException("A esteira está cheia.");
        }
        produtos.add(produto);
    }

    // A carga de silício também precisa caber na esteira.
    public double getCapacidadeSilicio() { return capacidadeSilicio; }
    public boolean verificarCarga(double quantidade) {
        if (!Double.isFinite(quantidade) || quantidade < 0)
            throw new IllegalArgumentException("Carga inválida.");
        double ocupada = porcao == null ? 0 : porcao.getQuantidade();
        for (Produto produto : produtos) ocupada += produto.getQuantidadeMateriaPrimaPorUnidade();
        return quantidade <= capacidadeSilicio - ocupada;
    }
    public void adicionarMateriaPrima(PorcaoMateriaPrima novaPorcao) {
        verificarMovimento();
        if (novaPorcao == null) throw new IllegalArgumentException("Informe a porção.");
        // Silício e chips não viajam juntos.
        if (porcao != null || !produtos.isEmpty() || !verificarCapacidade(1)
                || !verificarCarga(novaPorcao.getQuantidade()))
            throw new IllegalStateException("Esteira ocupada ou carga excessiva.");
        porcao = novaPorcao;
    }
    public PorcaoMateriaPrima removerMateriaPrima() {
        verificarMovimento();
        if (porcao == null) throw new IllegalStateException("Não há matéria-prima na esteira.");
        PorcaoMateriaPrima retirada = porcao;
        porcao = null;
        return retirada;
    }

    public Produto removerItem() {
        verificarMovimento();
        if (produtos.isEmpty()) {
            throw new IllegalStateException("A esteira está vazia.");
        }
        return produtos.remove(0);
    }

    private void verificarMovimento() {
        if (!emMovimento) {
            throw new IllegalStateException("A esteira está desligada.");
        }
    }
}
