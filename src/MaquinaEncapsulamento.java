// Protege o circuito e pode acrescentar risco ao produto.
public class MaquinaEncapsulamento extends Maquina {

    // Configura os parâmetros da etapa de encapsulamento.
    public MaquinaEncapsulamento() {
        super("Máquina de Encapsulamento", 1, 0.10, 5.0);
    }

    // Encapsula o produto e sorteia um possível aumento de risco.
    @Override
    public void processar(Produto produto) {
        validarProcessamento(produto, "Processado");
        if (verificarFalha(getProbabilidadeFalha())) {
            produto.aumentarProbabilidadeFalha(0.05);
        }
        produto.setStatus("Encapsulado");
    }

    // Identifica esta etapa da linha.
    @Override
    public String getTipo() {
        return "Encapsulamento";
    }
}
