package ca.yorku.greendriveev.catalog.exception;

public class InvalidCatalogQueryException extends RuntimeException {

    public InvalidCatalogQueryException(String message) {
        super(message);
    }
}