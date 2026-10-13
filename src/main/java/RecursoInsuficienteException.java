/**
 * Indica falta de silício ou orçamento para uma operação.
 * @author Giovanni Innecco e Rodrigo Soares
 */
public class RecursoInsuficienteException extends PlantaException {
    private static final long serialVersionUID = 1L;

    /**
     * Guarda o motivo para apresentar no menu.
     * @param mensagem motivo da interrupção
     */
    public RecursoInsuficienteException(String mensagem) {
        super(mensagem);
    }
}
