public class EstacaoInspecao {
    
    // Atributos
    private boolean ativa;
    private int produtosInspecionados;

    // Construtor
    public EstacaoInspecao() {
        this.ativa = false; // A estação nasce desativada por padrão
        this.produtosInspecionados = 0; // O contador de produtos começa zerado
    }

    // Métodos
    public void ativar() {
        this.ativa = true;
    }

    public void desativar() {
        this.ativa = false;
    }

    public void inspecionar(Produto produto) {

        if (!this.ativa) {
            System.out.println("[ERRO] A estação de inspeção está desativada. Não é possível realizar a inspeção.");
            return;
        }

        this.produtosInspecionados++;
        System.out.println("[OK] Produto " + produto.getNome() + " inspecionado e aprovado.");
    }

    // Métodos de acesso (getters)
    public int getTotalInspecionados() {
        return this.produtosInspecionados;
    }
}
