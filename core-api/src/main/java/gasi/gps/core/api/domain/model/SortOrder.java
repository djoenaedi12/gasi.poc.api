package gasi.gps.core.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Sort expression for search operations.
 *
 * <p>The {@code field} value is a public API field name. Implementations may
 * restrict which fields are sortable.</p>
 *
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
public class SortOrder {
    private String field;
    private Direction direction;

    /**
     * Creates an empty sort order.
     */
    public SortOrder() {
    }

    /**
     * Sort direction.
     *
     * @since 1.0.0
     */
    public enum Direction {
        /** Sort from lowest to highest. */
        ASC,
        /** Sort from highest to lowest. */
        DESC
    }

    /**
     * Creates an ascending sort order for a field.
     *
     * @param field public API field name
     * @return ascending sort order
     */
    public static SortOrder asc(String field) {
        return new SortOrder(field, Direction.ASC);
    }

    /**
     * Creates a descending sort order for a field.
     *
     * @param field public API field name
     * @return descending sort order
     */
    public static SortOrder desc(String field) {
        return new SortOrder(field, Direction.DESC);
    }
}
