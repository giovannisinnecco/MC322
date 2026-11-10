package br.unicamp.ic.mc322.model;

import br.unicamp.ic.mc322.exception.*;
/**
 * Representa os estados de operação de uma máquina.
 * @author Giovanni Innecco e Rodrigo Soares
 */
public enum EstadoMaquina {
    /** Máquina parada. */
    DESLIGADA,
    /** Máquina pronta para operar. */
    LIGADA,
    /** Máquina sem saúde para operar. */
    QUEBRADA
}
