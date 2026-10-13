/**
 * Indica equipamento parado, quebrado, ocupado ou incompatível com a carga.
 * @author Giovanni Innecco e Rodrigo Soares
 */
public class EquipamentoIndisponivelException extends PlantaException {
    private static final long serialVersionUID = 1L;

    /**
     * Guarda o motivo para apresentar no menu.
     * @param mensagem motivo da interrupção
     */
    public EquipamentoIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
