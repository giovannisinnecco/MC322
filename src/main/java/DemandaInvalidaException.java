/**
 * Indica um pedido inexistente ou uma transição que não pode ser feita.
 * @author Giovanni Innecco e Rodrigo Soares
 */
public class DemandaInvalidaException extends PlantaException {
    private static final long serialVersionUID = 1L;

    /**
     * Guarda o motivo para apresentar no menu.
     * @param mensagem motivo da interrupção
     */
    public DemandaInvalidaException(String mensagem) {
        super(mensagem);
    }
}
