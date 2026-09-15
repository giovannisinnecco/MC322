// Forma o circuito e pode acrescentar risco ao produto.
public class MaquinaFotolitografia extends Maquina {

    // Configura os parâmetros da etapa de fotolitografia.
    public MaquinaFotolitografia() {
        super("Máquina de Fotolitografia", 1, 0.20, 10.0);
    }

    // Processa o circuito e sorteia um possível aumento de risco.
    @Override
    public void processar(Produto produto) {
        validarProcessamento(produto, "Aguardando");
        produto.processar();
        if (verificarFalha(getProbabilidadeFalha())) {
            produto.aumentarProbabilidadeFalha(0.10);
        }
    }

    // Identifica esta etapa da linha.
    @Override
    public String getTipo() {
        return "Fotolitografia";
    }
}
