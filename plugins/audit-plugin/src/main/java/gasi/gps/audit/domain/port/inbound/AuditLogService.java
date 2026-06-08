package gasi.gps.audit.domain.port.inbound;

import gasi.gps.audit.application.dto.AuditLogDetailResponse;
import gasi.gps.audit.application.dto.AuditLogSummaryResponse;
import gasi.gps.core.api.domain.port.inbound.BaseReadService;

/**
 * Read service contract for audit logs.
 *
 * @since 1.0.0
 */
public interface AuditLogService extends BaseReadService<AuditLogSummaryResponse, AuditLogDetailResponse> {

}
