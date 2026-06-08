package gasi.gps.core.api.application.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * Base response DTO for list and pagination views.
 *
 * <p>Summary responses intentionally expose only the public encoded identifier
 * and creation timestamp shared by all resources.</p>
 *
 * @since 1.0.0
 */
@Data
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
public abstract class BaseSummaryResponse {

    /** Public encoded identifier exposed to API clients. */
    private String id;

    /** Timestamp when the resource was created. */
    private Instant createdAt;

    /**
     * Creates an empty base summary response.
     */
    protected BaseSummaryResponse() {
    }
}
