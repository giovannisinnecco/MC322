// Decide quais chips seguem para o armazém.
public class EstacaoInspecao extends Maquina {

    private int produtosInspecionados;

    public EstacaoInspecao() {
        this(10);
    }

    // A capacidade usa unidades de silício.
    public EstacaoInspecao(int capacidade) {
        super("Estação de Inspeção", capacidade, 0.05, 3.0);
        this.produtosInspecionados = 0;
    }

    @Override
    public void processar(Produto produto) {
        validarProcessamento(produto, StatusProduto.ENCAPSULADO);
        boolean rejeitado = verificarFalha(calcularChanceRejeicao(produto));
        boolean falhaInspecao = verificarFalha(getProbabilidadeFalha());
        if (falhaInspecao) {
            rejeitado = false;
        }
        produto.setStatus(rejeitado ? StatusProduto.REJEITADO : StatusProduto.APROVADO);
        produtosInspecionados++;
        registrarUso();
    }

    private double calcularChanceRejeicao(Produto produto) {
        return Math.min(1.0, produto.getQualidade()
                * (0.10 + produto.getProbabilidadeFalhaAcumulada()));
    }

    @Override
    public String getTipo() {
        return "Inspeção";
    }

    public int getTotalInspecionados() {
        return this.produtosInspecionados;
    }
}
