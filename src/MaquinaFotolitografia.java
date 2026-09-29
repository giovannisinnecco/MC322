// Forma o circuito e pode deixar defeitos no chip.
public class MaquinaFotolitografia extends Maquina {

    public MaquinaFotolitografia() {
        this(10);
    }

    // A capacidade usa unidades de silício.
    public MaquinaFotolitografia(int capacidade) {
        super("Máquina de Fotolitografia", capacidade, 0.20, 10.0);
    }

    @Override
    public void processar(Produto produto) {
        validarProcessamento(produto, StatusProduto.AGUARDANDO);
        produto.processar();
        if (verificarFalha(getProbabilidadeFalha())) {
            produto.aumentarProbabilidadeFalha(0.10);
        }
        registrarUso();
    }

    @Override
    public String getTipo() {
        return "Fotolitografia";
    }
}
