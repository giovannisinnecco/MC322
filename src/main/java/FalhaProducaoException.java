/**
 * Informa uma falha inesperada depois de desfazer a tentativa incompleta.
 * @author Giovanni Innecco e Rodrigo Soares
 */
public class FalhaProducaoException extends PlantaException {
    private static final long serialVersionUID = 1L;

    /**
     * Guarda o motivo para apresentar no menu.
     * @param mensagem motivo da interrupção
     */
    public FalhaProducaoException(String mensagem) {
        super(mensagem);
    }

    /**
     * Guarda também o erro original para diagnóstico.
     * @param mensagem motivo da interrupção
     * @param causa erro que interrompeu a operação
     */
    public FalhaProducaoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
