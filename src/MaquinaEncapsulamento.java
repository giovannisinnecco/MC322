// Protege o chip, mas pode deixar defeitos no caminho.
public class MaquinaEncapsulamento extends Maquina {

    public MaquinaEncapsulamento() {
        this(10);
    }

    // A capacidade usa unidades de silício.
    public MaquinaEncapsulamento(int capacidade) {
        super("Máquina de Encapsulamento", capacidade, 0.10, 5.0);
    }

    @Override
    public void processar(Produto produto) {
        validarProcessamento(produto, StatusProduto.PROCESSADO);
        if (verificarFalha(getProbabilidadeFalha())) {
            produto.aumentarProbabilidadeFalha(0.05);
        }
        produto.setStatus(StatusProduto.ENCAPSULADO);
        registrarUso();
    }

    @Override
    public String getTipo() {
        return "Encapsulamento";
    }
}
