package gasi.gps.audit.application.dto;

import gasi.gps.core.api.application.dto.BaseDetailResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Detail response DTO for single audit log entity views.
 * Contains all fields including audit metadata from {@link BaseDetailResponse}.
 *
 * @since 1.0.0
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AuditLogDetailResponse extends BaseDetailResponse {

    private String traceId;
    private String actorId;
    private String actorIp;
    private String action;
    private String module;
    private String resourceType;
    private String resourceId;
    private String description;
    @lombok.Builder.Default
    private String[] changedFields = new String[0];
    private String status;
}
