// Decide se cada unidade segue para o armazém.
public class EstacaoInspecao extends Maquina {

    // Registra quantas unidades passaram pela inspeção.
    private int produtosInspecionados;

    // Configura o custo e a chance de falha da estação.
    public EstacaoInspecao() {
        super("Estação de Inspeção", 1, 0.05, 3.0);
        this.produtosInspecionados = 0;
    }

    // Avalia o produto e aplica uma possível falha de detecção.
    @Override
    public void processar(Produto produto) {
        validarProcessamento(produto, "Encapsulado");
        boolean rejeitado = verificarFalha(calcularChanceRejeicao(produto));
        boolean falhaInspecao = verificarFalha(getProbabilidadeFalha());
        if (falhaInspecao) {
            rejeitado = false;
        }
        produto.setStatus(rejeitado ? "Rejeitado" : "Aprovado");
        produtosInspecionados++;
    }

    // Combina qualidade e risco na chance de rejeição.
    private double calcularChanceRejeicao(Produto produto) {
        return Math.min(1.0, produto.getQualidade()
                * (0.10 + produto.getProbabilidadeFalhaAcumulada()));
    }

    // Identifica esta etapa da linha.
    @Override
    public String getTipo() {
        return "Inspeção";
    }

    // Informa o total já inspecionado.
    public int getTotalInspecionados() {
        return this.produtosInspecionados;
    }
}
