package mirogaudi.productcatalog.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import mirogaudi.productcatalog.connector.ConnectorRuntimeException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * Error handler for controllers.
 */
@ControllerAdvice
@Slf4j
public class ControllerErrorHandler {

    @ExceptionHandler({
        MethodArgumentTypeMismatchException.class,
        MethodArgumentNotValidException.class,
        ConstraintViolationException.class
    })
    public ResponseEntity<Error> requestErrorHandler(Exception e) {
        return errorResponseEntity(BAD_REQUEST, e);
    }

    @ExceptionHandler({
        ConcurrencyFailureException.class,
        DataIntegrityViolationException.class
    })
    public ResponseEntity<Error> conflictErrorHandler(Exception e) {
        return errorResponseEntity(CONFLICT, e);
    }

    @ExceptionHandler({
        ConnectorRuntimeException.class
    })
    public ResponseEntity<Error> connectorErrorHandler(Exception e) {
        return errorResponseEntity(BAD_GATEWAY, e);
    }

    @ExceptionHandler({
        IllegalStateException.class,
        Throwable.class
    })
    public ResponseEntity<Error> defaultErrorHandler(Throwable t) {
        return errorResponseEntity(INTERNAL_SERVER_ERROR, t);
    }

    private ResponseEntity<Error> errorResponseEntity(HttpStatus httpStatus, Throwable t) {
        Error error = new Error(
            LocalDateTime.now(),
            httpStatus.value(),
            httpStatus.getReasonPhrase(),
            t.toString()
        );

        LOG.error("Error occurred: {}", error);

        return ResponseEntity.status(httpStatus).body(error);
    }

    /**
     * A record containing error details.
     */
    public record Error(@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
                        LocalDateTime timestamp,
                        int httpStatus,
                        String error,
                        String cause) {
    }

}
