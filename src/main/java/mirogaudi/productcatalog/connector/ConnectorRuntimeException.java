package mirogaudi.productcatalog.connector;

import java.io.Serial;

/**
 * Connector runtime exception.
 */
public class ConnectorRuntimeException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ConnectorRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

}
