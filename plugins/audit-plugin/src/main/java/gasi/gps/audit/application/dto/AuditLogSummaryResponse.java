package gasi.gps.audit.application.dto;

import gasi.gps.core.api.application.dto.BaseSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Summary response DTO for audit log list/pagination views.
 * Contains essential fields for quick overview.
 *
 * @since 1.0.0
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AuditLogSummaryResponse extends BaseSummaryResponse {

    private String actorId;
    private String action;
    private String module;
    private String resourceType;
    private String resourceId;
    @lombok.Builder.Default
    private String[] changedFields = new String[0];
    private String status;
}
