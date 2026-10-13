/**
 * Representa a etapa alcançada por um chip na linha.
 * @author Giovanni Innecco e Rodrigo Soares
 */
public enum StatusProduto {
    /** Chip ainda não processado. */
    AGUARDANDO,
    /** Circuito formado. */
    PROCESSADO,
    /** Chip protegido para inspeção. */
    ENCAPSULADO,
    /** Chip aceito no armazém. */
    APROVADO,
    /** Chip recusado na inspeção. */
    REJEITADO
}
