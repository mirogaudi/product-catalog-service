package mirogaudi.productcatalog.controller;

import lombok.extern.slf4j.Slf4j;
import mirogaudi.productcatalog.connector.ConnectorRuntimeException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * Error handler for rest controllers.
 */
@RestControllerAdvice
@Slf4j
public class RestControllerErrorHandler {

    /**
     * General error handler.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public Error generalErrorHandler(Exception e) {
        return error(e);
    }

    /**
     * Application error handler.
     */
    @ExceptionHandler({
            IllegalArgumentException.class,
            InvocationTargetException.class,
            RestClientException.class,
            ConversionFailedException.class,
            ClassCastException.class
    })
    @ResponseStatus(BAD_REQUEST)
    public Error applicationErrorHandler(Exception e) {
        return error(e);
    }

    /**
     * Connector error handler.
     */
    @ExceptionHandler(ConnectorRuntimeException.class)
    @ResponseStatus(BAD_GATEWAY)
    public Error connectorErrorHandler(Exception e) {
        return error(e);
    }

    /**
     * Concurrent modification error handler.
     */
    @ExceptionHandler({
            OptimisticLockingFailureException.class,
            DataIntegrityViolationException.class
    })
    @ResponseStatus(CONFLICT)
    public Error conflictErrorHandler(Exception e) {
        return error(e);
    }

    private Error error(Exception e) {
        Error error = new Error(
                e.toString(),
                NestedExceptionUtils.getMostSpecificCause(e).toString(),
                LocalDateTime.now()
        );

        LOG.error("Error occurred: {}", error);

        return error;
    }

    private record Error(String message,
                         String cause,
                         LocalDateTime timeStamp) {
    }

}
