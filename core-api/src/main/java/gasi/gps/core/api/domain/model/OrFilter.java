package gasi.gps.core.api.domain.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Filter expression that combines child filters with logical OR.
 *
 * <p>At least one child filter must match for a record to be included in the
 * result.</p>
 *
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class OrFilter extends GenericFilter {

    /** Child filter expressions where at least one must match. */
    private List<GenericFilter> filters;

    /**
     * Creates an empty OR filter.
     */
    public OrFilter() {
    }
}
