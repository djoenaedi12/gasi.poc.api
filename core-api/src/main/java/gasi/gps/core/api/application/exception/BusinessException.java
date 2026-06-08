package gasi.gps.core.api.application.exception;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Exception raised when one or more business rules are violated.
 *
 * <p>
 * Carries structured {@link ErrorDetail} items so each violation can expose a
 * machine-readable {@code code}, an optional {@code field}, and a
 * human-readable
 * {@code message}. Legacy string-based constructors and {@link #getErrors()}
 * are
 * retained for backward compatibility.
 * </p>
 *
 * @since 1.0.0
 */
public class BusinessException extends RuntimeException {

    /** Structured error details carried by this exception. */
    private final List<ErrorDetail> details;

    /**
     * Creates an exception with a single message (no code/field).
     *
     * @param message business error message
     */
    public BusinessException(String message) {
        super(message);
        this.details = List.of(ErrorDetail.of(null, message));
    }

    /**
     * Creates an exception from multiple plain messages (legacy, no code/field).
     *
     * @param errors business error messages
     */
    public BusinessException(List<String> errors) {
        super(String.join("; ", errors));
        this.details = errors.stream()
                .map(m -> ErrorDetail.of(null, m))
                .collect(Collectors.toList());
    }

    private BusinessException(List<ErrorDetail> details, boolean structured) {
        super(details.stream().map(ErrorDetail::getMessage).collect(Collectors.joining("; ")));
        this.details = List.copyOf(details);
    }

    /**
     * Creates an exception from structured error details.
     *
     * @param details structured error items
     * @return a new {@code BusinessException}
     */
    public static BusinessException of(List<ErrorDetail> details) {
        return new BusinessException(details, true);
    }

    /**
     * Creates an exception from a single structured error detail.
     *
     * @param detail structured error item
     * @return a new {@code BusinessException}
     */
    public static BusinessException of(ErrorDetail detail) {
        return of(List.of(detail));
    }

    /**
     * Returns the error messages only (backward compatible).
     *
     * @return immutable list of messages
     */
    public List<String> getErrors() {
        return details.stream()
                .map(ErrorDetail::getMessage)
                .collect(Collectors.toList());
    }

    /**
     * Returns the structured error details.
     *
     * @return immutable list of {@link ErrorDetail}
     */
    public List<ErrorDetail> getErrorDetails() {
        return List.copyOf(details);
    }

    /**
     * Collects validation errors before throwing one {@link BusinessException}.
     */
    public static class Collector {

        private final List<ErrorDetail> details = new ArrayList<>();

        /**
         * Creates an empty business exception collector.
         */
        public Collector() {
        }

        /**
         * Adds a plain message error (no code/field).
         *
         * @param error business error message
         * @return this collector
         */
        public Collector add(String error) {
            details.add(ErrorDetail.of(null, error));
            return this;
        }

        /**
         * Adds a structured error detail.
         *
         * @param detail structured error item
         * @return this collector
         */
        public Collector add(ErrorDetail detail) {
            details.add(detail);
            return this;
        }

        /**
         * Adds a plain message error when a condition is {@code true}.
         *
         * @param condition validation-failure condition
         * @param error     business error message
         * @return this collector
         */
        public Collector addIf(boolean condition, String error) {
            if (condition) {
                details.add(ErrorDetail.of(null, error));
            }
            return this;
        }

        /**
         * Adds a structured error detail when a condition is {@code true}.
         *
         * @param condition validation-failure condition
         * @param detail    structured error item
         * @return this collector
         */
        public Collector addIf(boolean condition, ErrorDetail detail) {
            if (condition) {
                details.add(detail);
            }
            return this;
        }

        /**
         * Indicates whether any errors were collected.
         *
         * @return {@code true} when at least one error exists
         */
        public boolean hasErrors() {
            return !details.isEmpty();
        }

        /**
         * Throws {@link BusinessException} when any errors were collected.
         */
        public void validate() {
            if (hasErrors()) {
                throw BusinessException.of(details);
            }
        }
    }
}
