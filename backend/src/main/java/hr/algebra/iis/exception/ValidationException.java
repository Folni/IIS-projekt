package hr.algebra.iis.exception;

import java.util.List;

/**
 * Bacamo kad XML ili JSON validacija ne prođe.
 * Sadrži listu grešaka za prikaz korisniku.
 */
public class ValidationException extends Exception {
    private final List<String> errors;

    public ValidationException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
