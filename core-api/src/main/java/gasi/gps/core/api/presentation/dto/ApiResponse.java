package gasi.gps.core.api.presentation.dto;

import java.time.Instant;
import java.util.List;

import gasi.gps.core.api.application.exception.ErrorDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Standard API response envelope for all REST endpoints.
 *
 * <h2>Success example:</h2>
 *
 * <pre>{@code
 * {
 * "success": true,
 * "message": "OK",
 * "data": { ... },
 * "timestamp": "2026-03-03T10:15:30Z"
 * }
 * }</pre>
 *
 * <h2>Error example:</h2>
 *
 * <pre>{@code
 * {
 * "success": false,
 * "status": 422,
 * "message": "Business rule violation",
 * "errors": [
 * { "code": "PERSON_IDENTITY_PRIMARY_EXISTS", "field": "primaryIdentity",
 * "message": "Primary identity already exists" }
 * ],
 * "timestamp": "2026-03-03T10:15:30Z"
 * }
 * }</pre>
 *
 * <p>
 * All errors share a single {@code errors} array of {@link ErrorDetail} items
 * ({@code code}, optional {@code field}, {@code message}). The previous
 * {@code fieldErrors} map and string-based {@code errors} list have been
 * unified into this shape.
 * </p>
 *
 * @param <T> the data payload type
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private Integer status;
    private String message;
    private T data;
    private List<ErrorDetail> errors;

    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * Creates an empty API response envelope.
     */
    public ApiResponse() {
    }

    /**
     * Creates a success response with data.
     *
     * @param data the response payload
     * @param <T>  the payload type
     * @return a success {@code ApiResponse}
     */
    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("OK")
                .data(data)
                .build();
    }

    /**
     * Creates a success response with data and a custom message.
     *
     * @param data    the response payload
     * @param message a custom success message
     * @param <T>     the payload type
     * @return a success {@code ApiResponse}
     */
    public static <T> ApiResponse<T> ok(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Creates a success response with no payload.
     *
     * @return a no-content {@code ApiResponse}
     */
    public static ApiResponse<Void> noContent() {
        return ApiResponse.<Void>builder()
                .success(true)
                .message("No Content")
                .build();
    }

    /**
     * Creates an error response with a single summary message (no field/code).
     *
     * @param status  HTTP/application status code stored on the envelope
     * @param message the error summary
     * @param <T>     the payload type
     * @return an error {@code ApiResponse}
     */
    public static <T> ApiResponse<T> error(int status, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .status(status)
                .message(message)
                .errors(List.of(ErrorDetail.of(null, message)))
                .build();
    }

    /**
     * Creates an error response with detailed, machine-readable error items.
     *
     * @param status  HTTP/application status code stored on the envelope
     * @param message the error summary
     * @param errors  the structured error items
     * @param <T>     the payload type
     * @return an error {@code ApiResponse}
     */
    public static <T> ApiResponse<T> error(int status, String message, List<ErrorDetail> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .status(status)
                .message(message)
                .errors(errors == null ? List.of() : List.copyOf(errors))
                .build();
    }
}
