package com.checkpoint.api.exceptions;

/**
 * Exception thrown when a user tries to update their pseudo to one
 * that is already taken by another user.
 */
public class PseudoAlreadyExistsException extends RuntimeException {

    /**
     * Constructs a new PseudoAlreadyExistsException.
     *
     * @param pseudo the pseudo that is already taken
     */
    public PseudoAlreadyExistsException(String pseudo) {
        super("Pseudo '" + pseudo + "' is already taken");
    }
}
