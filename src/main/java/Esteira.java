import java.util.ArrayList;

/**
 * Transporta silício ou chips, respeitando movimento, ocupação e carga.
 * @author Giovanni Innecco e Rodrigo Soares
 */
public class Esteira {

    private final ArrayList<Produto> produtos = new ArrayList<>();
    private boolean emMovimento;
    private final int capacidadeMaxima;
    private final double capacidadeSilicio;
    private PorcaoMateriaPrima porcao;

    /**
     * Cria o componente com os valores padrão da fábrica.
     */
    public Esteira() {
        this(1);
    }

    /**
     * Cria uma esteira parada com os limites informados.
     * @param capacidadeMaxima capacidade positiva do equipamento
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    public Esteira(int capacidadeMaxima) {
        this(capacidadeMaxima, 10);
    }

    /**
     * Cria uma esteira parada com os limites informados.
     * @param capacidadeMaxima capacidade positiva do equipamento
     * @param capacidadeSilicio carga máxima finita e positiva em silício
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
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

    /**
     * Habilita o equipamento para operar.
     */
    public void ligar() { emMovimento = true; }
    /**
     * Interrompe a operação do equipamento.
     */
    public void desligar() { emMovimento = false; }
    /**
     * Consulta true se a esteira está ligada.
     * @return true se a esteira está ligada
     */
    public boolean estaEmMovimento() { return emMovimento; }
    /**
     * Consulta capacidade máxima do equipamento.
     * @return capacidade máxima do equipamento
     */
    public int getCapacidadeMaxima() { return capacidadeMaxima; }
    /**
     * Consulta o número de chips na fila.
     * @return quantidade de chips
     */
    public int getQuantidadeProdutos() { return produtos.size(); }

    /**
     * Verifica se há capacidade para a quantidade informada.
     * @param quantidadeAdicional quantidade a acrescentar
     * @return true se a quantidade cabe
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    public boolean verificarCapacidade(int quantidadeAdicional) {
        if (quantidadeAdicional < 0) {
            throw new IllegalArgumentException("A quantidade adicional não pode ser negativa.");
        }
        return quantidadeAdicional <= capacidadeMaxima - produtos.size() - (porcao == null ? 0 : 1);
    }

    /**
     * Coloca um chip na esteira ligada, sem misturar com silício bruto.
     * @param produto chip recebido pela etapa
     * @throws EquipamentoIndisponivelException se o estado, a etapa ou a capacidade impedir a operação
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    public void adicionarItem(Produto produto) {
        verificarMovimento();
        if (produto == null) {
            throw new IllegalArgumentException("O produto deve ser informado.");
        }
        if (porcao != null) throw new EquipamentoIndisponivelException("Esteira transportando matéria-prima.");
        if (produtos.contains(produto)) {
            throw new EquipamentoIndisponivelException("Esse produto já está na esteira.");
        }
        if (!verificarCapacidade(1) || !verificarCarga(produto.getQuantidadeMateriaPrimaPorUnidade())) {
            throw new EquipamentoIndisponivelException("A esteira está cheia.");
        }
        produtos.add(produto);
    }

    // A carga de silício também precisa caber na esteira.
    /**
     * Consulta carga máxima em unidades de silício.
     * @return carga máxima em unidades de silício
     */
    public double getCapacidadeSilicio() { return capacidadeSilicio; }
    /**
     * Confere a carga adicional em silício, incluindo os itens presentes.
     * @param quantidade quantidade solicitada
     * @return true se a carga adicional cabe
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    public boolean verificarCarga(double quantidade) {
        if (!Double.isFinite(quantidade) || quantidade < 0)
            throw new IllegalArgumentException("Carga inválida.");
        double ocupada = porcao == null ? 0 : porcao.getQuantidade();
        for (Produto produto : produtos) ocupada += produto.getQuantidadeMateriaPrimaPorUnidade();
        return quantidade <= capacidadeSilicio - ocupada;
    }
    /**
     * Coloca uma porção na esteira ligada e vazia.
     * @param novaPorcao porção de silício que será transportada
     * @throws EquipamentoIndisponivelException se o estado, a etapa ou a capacidade impedir a operação
     * @throws IllegalArgumentException se algum valor recebido for nulo ou estiver fora dos limites aceitos
     */
    public void adicionarMateriaPrima(PorcaoMateriaPrima novaPorcao) {
        verificarMovimento();
        if (novaPorcao == null) throw new IllegalArgumentException("Informe a porção.");
        // Silício e chips não viajam juntos.
        if (porcao != null || !produtos.isEmpty() || !verificarCapacidade(1)
                || !verificarCarga(novaPorcao.getQuantidade()))
            throw new EquipamentoIndisponivelException("Esteira ocupada ou carga excessiva.");
        porcao = novaPorcao;
    }
    /**
     * Entrega a porção transportada e libera a esteira.
     * @return porção que estava na esteira
     * @throws EquipamentoIndisponivelException se o estado, a etapa ou a capacidade impedir a operação
     */
    public PorcaoMateriaPrima removerMateriaPrima() {
        verificarMovimento();
        if (porcao == null) throw new EquipamentoIndisponivelException("Não há matéria-prima na esteira.");
        PorcaoMateriaPrima retirada = porcao;
        porcao = null;
        return retirada;
    }

    /**
     * Entrega o primeiro chip da fila com a esteira em movimento.
     * @return primeiro chip transportado
     * @throws EquipamentoIndisponivelException se o estado, a etapa ou a capacidade impedir a operação
     */
    public Produto removerItem() {
        verificarMovimento();
        if (produtos.isEmpty()) {
            throw new EquipamentoIndisponivelException("A esteira está vazia.");
        }
        return produtos.remove(0);
    }

    // Retira os itens de uma tentativa interrompida.
    /**
     * Libera a esteira ao interromper uma tentativa.
     */
    void esvaziar() {
        produtos.clear();
        porcao = null;
    }

    /**
     * Impede transporte enquanto a esteira estiver parada.
     * @throws EquipamentoIndisponivelException se o estado, a etapa ou a capacidade impedir a operação
     */
    private void verificarMovimento() {
        if (!emMovimento) {
            throw new EquipamentoIndisponivelException("A esteira está desligada.");
        }
    }
}
