package gasi.gps.audit.application.service;

import gasi.gps.audit.application.dto.AuditLogDetailResponse;
import gasi.gps.audit.application.dto.AuditLogSummaryResponse;
import gasi.gps.audit.application.mapper.AuditLogDtoMapper;
import gasi.gps.audit.domain.model.AuditLog;
import gasi.gps.audit.domain.port.inbound.AuditLogService;
import gasi.gps.audit.domain.port.outbound.AuditLogRepositoryPort;
import gasi.gps.core.starter.application.service.BaseReadServiceImpl;
import gasi.gps.core.starter.infrastructure.i18n.MessageUtil;
import gasi.gps.core.starter.infrastructure.util.IdEncoder;

/**
 * Default read service implementation for audit logs.
 *
 * @since 1.0.0
 */
public class AuditLogServiceImpl extends BaseReadServiceImpl<AuditLog, AuditLogSummaryResponse, AuditLogDetailResponse>
        implements AuditLogService {

    /**
     * Creates an audit log service implementation.
     *
     * @param repositoryPort audit log repository
     * @param mapper         audit log DTO mapper
     * @param messageUtil    message resolver
     * @param idEncoder      encoded ID codec
     */
    public AuditLogServiceImpl(AuditLogRepositoryPort repositoryPort,
            AuditLogDtoMapper mapper,
            MessageUtil messageUtil, IdEncoder idEncoder) {
        super(repositoryPort, mapper, messageUtil, idEncoder);
    }

    @Override
    protected String resourceType() {
        return "AuditLog";
    }
}
