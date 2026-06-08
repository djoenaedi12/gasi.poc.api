package gasi.gps.core.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Filter expression for a single searchable field.
 *
 * <p>The {@code field} value is a public API field name. Implementations may
 * restrict which fields are accepted, for example by requiring entity fields to
 * be explicitly marked as filterable.</p>
 *
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SimpleFilter extends GenericFilter {

    /** Public API field name to filter. */
    private String field;

    /** Operator applied to the field value. */
    private FilterOperator operator;

    /** Comparison value for operators that require one. */
    private Object value;

    /**
     * Creates an empty simple filter.
     */
    public SimpleFilter() {
    }

    /**
     * Supported operators for {@link SimpleFilter}.
     *
     * @since 1.0.0
     */
    public enum FilterOperator {
        /** Field value must equal {@code value}. */
        EQUALS,
        /** Field value must not equal {@code value}. */
        NOT_EQUALS,
        /** Field value must be greater than {@code value}. */
        GREATER_THAN,
        /** Field value must be greater than or equal to {@code value}. */
        GREATER_THAN_OR_EQUALS,
        /** Field value must be less than {@code value}. */
        LESS_THAN,
        /** Field value must be less than or equal to {@code value}. */
        LESS_THAN_OR_EQUALS,
        /** Field value must match a contains-style string pattern. */
        LIKE,
        /** Field value must be included in a collection value. */
        IN,
        /** Field value must be {@code null}. */
        IS_NULL,
        /** Field value must not be {@code null}. */
        IS_NOT_NULL
    }
}
