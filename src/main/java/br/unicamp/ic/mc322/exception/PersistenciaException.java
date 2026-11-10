package br.unicamp.ic.mc322.exception;

/** Indica falha ao salvar ou reconstruir uma fábrica persistida. */
public class PersistenciaException extends RuntimeException {
    /**
     * Cria uma falha com descrição e causa original.
     * @param mensagem explicação legível da falha
     * @param causa exceção original, quando disponível
     */
    public PersistenciaException(String mensagem, Throwable causa) { super(mensagem, causa); }
    /**
     * Cria uma falha de validação do arquivo.
     * @param mensagem explicação da falha de validação
     */
    public PersistenciaException(String mensagem) { super(mensagem); }
}
