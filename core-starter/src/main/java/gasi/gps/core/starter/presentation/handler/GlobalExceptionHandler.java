package gasi.gps.core.starter.presentation.handler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import gasi.gps.core.api.application.exception.BusinessException;
import gasi.gps.core.api.application.exception.EntityNotFoundException;
import gasi.gps.core.api.application.exception.ErrorDetail;
import gasi.gps.core.api.presentation.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import tools.jackson.core.JacksonException;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.exc.InvalidTypeIdException;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

/**
 * Global exception handler that translates exceptions into standard
 * {@link ApiResponse} error envelopes.
 *
 * <p>
 * Every error is normalized to a single {@code errors} array of
 * {@link ErrorDetail} items ({@code code}, optional {@code field},
 * {@code message}),
 * so clients handle one consistent shape across validation, business, and
 * transport errors.
 * </p>
 *
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Creates a global exception handler.
     */
    public GlobalExceptionHandler() {
    }

    /**
     * Handles entity not found (404).
     *
     * @param ex exception thrown when an entity cannot be found
     * @return API error response
     */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleEntityNotFound(EntityNotFoundException ex) {
        LOG.warn("Entity not found: {}", ex.getMessage());
        return ApiResponse.error(HttpStatus.NOT_FOUND.value(), "Entity not found",
                List.of(ErrorDetail.of("ENTITY_NOT_FOUND", ex.getMessage())));
    }

    /**
     * Handles requests to non-existent endpoints (404).
     *
     * @param ex exception thrown when no request handler exists
     * @return API error response
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNoHandlerFound(NoHandlerFoundException ex) {
        String message = String.format("No endpoint found: %s %s", ex.getHttpMethod(), ex.getRequestURL());
        LOG.warn("No handler found: {}", message);
        return ApiResponse.error(HttpStatus.NOT_FOUND.value(), "No endpoint found",
                List.of(ErrorDetail.of("NO_HANDLER", message)));
    }

    /**
     * Handles unsupported HTTP method requests (405).
     *
     * @param ex exception thrown for unsupported HTTP methods
     * @return API error response
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiResponse<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String supported = ex.getSupportedHttpMethods() != null
                ? ex.getSupportedHttpMethods().toString()
                : "unknown";
        String message = String.format("Method '%s' is not supported. Supported methods: %s",
                ex.getMethod(), supported);
        LOG.warn("Method not supported: {}", message);
        return ApiResponse.error(HttpStatus.METHOD_NOT_ALLOWED.value(), "Method not allowed",
                List.of(ErrorDetail.of("METHOD_NOT_ALLOWED", message)));
    }

    /**
     * Handles business rule violations (422).
     *
     * @param ex exception containing business validation errors
     * @return API error response
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ApiResponse<Void> handleBusinessException(BusinessException ex) {
        LOG.warn("Business rule violation: {}", ex.getMessage());
        return ApiResponse.error(HttpStatus.UNPROCESSABLE_CONTENT.value(),
                "Business rule violation", ex.getErrorDetails());
    }

    /**
     * Handles database integrity violations such as unique-key or
     * foreign-key constraint failures (409).
     *
     * @param ex data integrity violation from the persistence layer
     * @return API error response
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        LOG.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return ApiResponse.error(HttpStatus.CONFLICT.value(), "Data conflicts with an existing record",
                List.of(ErrorDetail.of("DATA_CONFLICT", "A unique or reference constraint was violated")));
    }

    /**
     * Handles concurrent-modification conflicts from optimistic locking (409).
     *
     * @param ex optimistic locking failure
     * @return API error response
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        LOG.warn("Optimistic lock conflict: {}", ex.getMessage());
        return ApiResponse.error(HttpStatus.CONFLICT.value(),
                "Record was modified by someone else; reload and try again",
                List.of(ErrorDetail.of("OPTIMISTIC_LOCK_CONFLICT", "The record was changed by another request")));
    }

    /**
     * Handles failed authentication (wrong username/password, locked, disabled)
     * (401).
     *
     * @param ex authentication failure
     * @return API error response
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleAuthenticationFailed(AuthenticationException ex) {
        LOG.warn("Authentication failed: {}", ex.getMessage());
        return ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "Invalid credentials",
                List.of(ErrorDetail.of("UNAUTHORIZED", "Invalid credentials")));
    }

    /**
     * Handles authorization denied errors from Spring Security (401 or 403).
     *
     * <p>
     * Returns 401 if the current user is not authenticated (anonymous),
     * 403 if authenticated but lacks the required permission.
     * </p>
     *
     * @param ex authorization failure
     * @return API error response with the appropriate HTTP status
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthorizationDenied(AuthorizationDeniedException ex) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAnonymous = auth == null || auth instanceof AnonymousAuthenticationToken;

        if (isAnonymous) {
            LOG.warn("Unauthenticated access denied");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "Unauthorized",
                            List.of(ErrorDetail.of("UNAUTHORIZED", "Unauthorized"))));
        }
        LOG.warn("Authorization denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), "Access denied",
                        List.of(ErrorDetail.of("ACCESS_DENIED", "Access denied"))));
    }

    /**
     * Handles bean validation errors from {@code @Valid} request bodies (400).
     *
     * @param ex method argument validation failure
     * @return API error response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex) {
        List<ErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> ErrorDetail.of(fe.getCode(), fe.getField(), fe.getDefaultMessage()))
                .toList();
        LOG.warn("Validation failed: {}", errors);
        return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Validation failed", errors);
    }

    /**
     * Handles bean validation errors from method/parameter constraints on
     * {@code @Validated} beans (400).
     *
     * @param ex constraint violation failure
     * @return API error response
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleConstraintViolation(ConstraintViolationException ex) {
        List<ErrorDetail> errors = ex.getConstraintViolations().stream()
                .map(v -> ErrorDetail.of(
                        v.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(),
                        lastNode(v.getPropertyPath()),
                        v.getMessage()))
                .toList();
        LOG.warn("Constraint violation: {}", errors);
        return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Validation failed", errors);
    }

    /**
     * Handles controller method-parameter validation errors (Spring 6.1+) (400).
     *
     * @param ex handler method validation failure
     * @return API error response
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleHandlerMethodValidation(HandlerMethodValidationException ex) {
        List<ErrorDetail> errors = ex.getAllErrors().stream()
                .map(err -> {
                    String[] codes = err.getCodes();
                    String code = codes != null && codes.length > 0 ? codes[codes.length - 1] : null;
                    String field = err instanceof FieldError fe ? fe.getField() : null;
                    return ErrorDetail.of(code, field, err.getDefaultMessage());
                })
                .toList();
        LOG.warn("Method validation failed: {}", errors);
        return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Validation failed", errors);
    }

    /**
     * Handles malformed JSON or unreadable request body (400).
     *
     * @param ex unreadable request body exception
     * @return API error response
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        String detail = extractReadableDetail(ex);
        LOG.warn("Malformed request body: {}", detail);
        return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Malformed request body",
                List.of(ErrorDetail.of("MALFORMED_BODY", detail)));
    }

    /**
     * Handles type mismatch in path variables or request params (400).
     *
     * @param ex type mismatch exception
     * @return API error response
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Parameter '%s' must be of type %s",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        LOG.warn("Type mismatch: {}", message);
        return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Type mismatch",
                List.of(ErrorDetail.of("TYPE_MISMATCH", ex.getName(), message)));
    }

    /**
     * Catches any unhandled exception (500).
     *
     * @param ex unhandled exception
     * @return API error response
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleGeneric(Exception ex) {
        LOG.error("Unexpected error", ex);
        return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred",
                List.of(ErrorDetail.of("INTERNAL_ERROR", "An unexpected error occurred")));
    }

    /**
     * Extracts a human-readable detail message from the root cause of
     * an {@link HttpMessageNotReadableException}.
     *
     * @param ex the unreadable request body exception
     * @return a readable detail message
     */
    private String extractReadableDetail(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        LOG.debug("HttpMessageNotReadableException cause type: {}, message: {}",
                cause != null ? cause.getClass().getName() : "null",
                cause != null ? cause.getMessage() : ex.getMessage());

        if (cause instanceof InvalidTypeIdException itid) {
            String path = buildJsonPath(itid.getPath());
            return String.format("Unknown type '%s' at '%s'", itid.getTypeId(), path);
        }

        if (cause instanceof UnrecognizedPropertyException upe) {
            String path = buildJsonPath(upe.getPath());
            return String.format("Unrecognized field '%s' at '%s'", upe.getPropertyName(), path);
        }

        if (cause instanceof MismatchedInputException mie) {
            String path = buildJsonPath(mie.getPath());
            return String.format("Invalid value at '%s'", path);
        }

        if (cause instanceof StreamReadException sre) {
            return String.format("Invalid JSON syntax at line %d, column %d: %s",
                    sre.getLocation().getLineNr(),
                    sre.getLocation().getColumnNr(),
                    sre.getOriginalMessage());
        }

        return "Malformed request body";
    }

    /**
     * Builds a dotted JSON path string from Jackson's reference list,
     * e.g. {@code "filter.filters[1].type"}.
     *
     * @param path the Jackson reference path
     * @return a dotted path string
     */
    private String buildJsonPath(List<JacksonException.Reference> path) {
        if (path == null || path.isEmpty()) {
            return "$";
        }
        StringBuilder sb = new StringBuilder();
        for (JacksonException.Reference ref : path) {
            if (ref.getIndex() >= 0) {
                sb.append('[').append(ref.getIndex()).append(']');
            } else {
                if (!sb.isEmpty()) {
                    sb.append('.');
                }
                sb.append(ref.getPropertyName());
            }
        }
        return sb.toString();
    }

    /**
     * Returns the leaf property name from a jakarta validation path.
     *
     * @param path the constraint violation property path
     * @return the leaf node name, or {@code null} when empty
     */
    private String lastNode(Path path) {
        String field = null;
        for (Path.Node node : path) {
            field = node.getName();
        }
        return field;
    }
}
