package gasi.gps.core.api.application.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Structured, machine-readable error item used across all error responses.
 *
 * <p>
 * A single canonical shape replaces the previous split between a flat list of
 * messages and a field-keyed map. {@code code} is a stable
 * application/validation
 * code (e.g. {@code "NotBlank"}, {@code "PERSON_IDENTITY_PRIMARY_EXISTS"}),
 * {@code field} is optional (null for global errors), and {@code message} is
 * the
 * human-readable text.
 * </p>
 *
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
public class ErrorDetail {

    /** Stable machine-readable code. */
    private String code;

    /** Optional field name; {@code null} for non-field (global) errors. */
    private String field;

    /** Human-readable message (may be server-resolved i18n text). */
    private String message;

    /**
     * Creates an empty error detail.
     */
    public ErrorDetail() {
    }

    /**
     * Creates a global (non-field) error detail.
     *
     * @param code    machine-readable code (nullable)
     * @param message human-readable message
     * @return a new {@code ErrorDetail}
     */
    public static ErrorDetail of(String code, String message) {
        return new ErrorDetail(code, null, message);
    }

    /**
     * Creates a field-scoped error detail.
     *
     * @param code    machine-readable code (nullable)
     * @param field   field name
     * @param message human-readable message
     * @return a new {@code ErrorDetail}
     */
    public static ErrorDetail of(String code, String field, String message) {
        return new ErrorDetail(code, field, message);
    }
}
