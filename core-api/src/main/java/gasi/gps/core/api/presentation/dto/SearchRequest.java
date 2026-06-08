package gasi.gps.core.api.presentation.dto;

import java.util.List;

import gasi.gps.core.api.domain.model.GenericFilter;
import gasi.gps.core.api.domain.model.SortOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;

/**
 * Request body for search endpoints.
 *
 * <p>The request combines an optional polymorphic filter, optional sort orders,
 * and optional page settings. Field names are public API field names and may be
 * restricted by the persistence adapter.</p>
 *
 * <p>
 * Example JSON:
 * </p>
 *
 * <pre>{@code
 * {
 * "filter": {
 * "type": "simple",
 * "field": "name",
 * "operator": "LIKE",
 * "value": "admin"
 * },
 * "sorts": [
 * { "field": "createdAt", "direction": "DESC" }
 * ],
 * "fields": ["id", "code", "name"]
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
public class SearchRequest {
    /** Default zero-based page index. */
    public static final int DEFAULT_PAGE = 0;

    /** Default page size used when the request omits or passes an invalid size. */
    public static final int DEFAULT_SIZE = 10;

    /** Maximum page size accepted by the API. */
    public static final int MAX_SIZE = 100;

    private GenericFilter filter;
    private List<SortOrder> sorts;
    /**
     * Optional response projection for search list/page endpoints.
     *
     * <p>When provided, only these public DTO fields are returned. Controllers
     * may force required fields such as {@code id} to remain present.</p>
     */
    private List<String> fields;
    @Default
    private Integer page = DEFAULT_PAGE;
    @Default
    private Integer size = DEFAULT_SIZE;

    /**
     * Creates an empty search request.
     */
    public SearchRequest() {
    }

    /**
     * Returns the requested page normalized to the supported lower bound.
     *
     * @return zero-based page index, defaulting to {@link #DEFAULT_PAGE}
     */
    public int normalizedPage() {
        return page == null || page < DEFAULT_PAGE ? DEFAULT_PAGE : page;
    }

    /**
     * Returns the requested page size normalized to the supported range.
     *
     * @return page size between {@code 1} and {@link #MAX_SIZE}, defaulting to
     *         {@link #DEFAULT_SIZE}
     */
    public int normalizedSize() {
        if (size == null || size <= 0) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }
}
