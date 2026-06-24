package gasi.gps.core.api.application.dto;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * Base response DTO for single-resource detail views.
 *
 * <p>
 * Extends {@link BaseSummaryResponse} with audit metadata and optimistic
 * locking version. The public {@code id} remains encoded in the superclass.
 * </p>
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

    /** Optional custom field values keyed by field code. */
    @Builder.Default
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Object> customFields = Map.of();

    /**
     * Creates an empty base detail response.
     */
    protected BaseDetailResponse() {
    }
}
