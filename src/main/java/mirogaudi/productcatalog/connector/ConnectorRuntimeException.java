package mirogaudi.productcatalog.connector;

/**
 * Connector runtime exception.
 */
public class ConnectorRuntimeException extends RuntimeException {

    public ConnectorRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

}
