package gasi.gps.core.api.application.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * Base response DTO for single-resource detail views.
 *
 * <p>Extends {@link BaseSummaryResponse} with audit metadata and optimistic
 * locking version. The public {@code id} remains encoded in the superclass.</p>
 *
 * @since 1.0.0
 */
@Data
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class BaseDetailResponse extends BaseSummaryResponse {

    /** Timestamp when the resource was last updated. */
    private Instant updatedAt;

    /** User or system actor that created the resource. */
    private String createdBy;

    /** User or system actor that last updated the resource. */
    private String updatedBy;

    /** Optimistic locking version. */
    private Integer version;

    /**
     * Creates an empty base detail response.
     */
    protected BaseDetailResponse() {
    }
}
