package gasi.gps.audit.presentation.controller;

import java.util.List;

import gasi.gps.audit.application.dto.AuditLogDetailResponse;
import gasi.gps.audit.application.dto.AuditLogSummaryResponse;
import gasi.gps.audit.domain.port.inbound.AuditLogService;
import gasi.gps.core.starter.presentation.controller.BaseReadController;
import gasi.gps.core.starter.infrastructure.util.IdEncoder;

/**
 * REST controller for audit log read/search endpoints.
 *
 * @since 1.0.0
 */
public class AuditLogController extends BaseReadController<AuditLogSummaryResponse, AuditLogDetailResponse> {

    /**
     * Creates an audit log controller.
     *
     * @param service   audit log read service
     * @param idEncoder encoded ID codec
     */
    public AuditLogController(AuditLogService service, IdEncoder idEncoder) {
        super(service, idEncoder);
    }

    @Override
    public String getResourceName() {
        return "AuditLog";
    }

    @Override
    protected List<String> getDefaultSummaryFields() {
        return List.of(
                "id",
                "createdAt",
                "actorId",
                "action",
                "module",
                "resourceType",
                "resourceId",
                "changedFields",
                "status");
    }
}
