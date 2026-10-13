/**
 * Representa uma operação que a fábrica não conseguiu realizar.
 * @author Giovanni Innecco e Rodrigo Soares
 */
public class PlantaException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    /**
     * Guarda o motivo para apresentar no menu.
     * @param mensagem motivo da interrupção
     */
    public PlantaException(String mensagem) {
        super(mensagem);
    }

    /**
     * Guarda também o erro original para diagnóstico.
     * @param mensagem motivo da interrupção
     * @param causa erro que interrompeu a operação
     */
    public PlantaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
