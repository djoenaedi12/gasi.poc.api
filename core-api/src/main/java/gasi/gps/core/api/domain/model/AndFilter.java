package gasi.gps.core.api.domain.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Filter expression that combines child filters with logical AND.
 *
 * <p>All child filters must match for a record to be included in the result.</p>
 *
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AndFilter extends GenericFilter {

    /** Child filter expressions that must all match. */
    private List<GenericFilter> filters;

    /**
     * Creates an empty AND filter.
     */
    public AndFilter() {
    }
}
