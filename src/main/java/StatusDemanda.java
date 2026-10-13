/**
 * Representa a situação de um pedido ao longo da fabricação.
 * @author Giovanni Innecco e Rodrigo Soares
 */
public enum StatusDemanda {
    /** Pedido esperando seleção. */
    PENDENTE("Pendente"),
    /** Pedido em fabricação. */
    EM_PRODUCAO("Em produção"),
    /** Pedido sem saldo pendente. */
    CONCLUIDA("Concluída"),
    /** Saldo interrompido por falta de recursos. */
    CANCELADA("Cancelada");
    private final String descricao;
    /**
     * Associa o estado ao texto usado no menu.
     * @param descricao texto para o menu
     */
    StatusDemanda(String descricao) { this.descricao = descricao; }
    /**
     * Consulta descrição para menus e relatórios.
     * @return descrição para menus e relatórios
     */
    public String getDescricao() { return descricao; }
}
