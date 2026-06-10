package gasi.gps.audit.infrastructure.adapter;

import java.time.Instant;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import gasi.gps.audit.domain.model.AuditLog;
import gasi.gps.audit.domain.port.outbound.AuditLogRepositoryPort;
import gasi.gps.audit.infrastructure.entity.AuditLogEntity;
import gasi.gps.audit.infrastructure.mapper.AuditLogMapper;
import gasi.gps.audit.infrastructure.persistence.AuditLogEntityRepository;
import gasi.gps.core.api.audit.AuditLogSpi;
import gasi.gps.core.api.security.SecurityContextProvider;
import gasi.gps.core.starter.infrastructure.adapter.BaseRepositoryAdapter;

/**
 * Adapter that implements {@link AuditLogSpi} from core-api,
 * allowing other plugins to manually write audit logs.
 *
 * @since 1.0.0
 */
@Component
public class AuditLogRepositoryAdapter extends BaseRepositoryAdapter<AuditLog, AuditLogEntity>
        implements AuditLogRepositoryPort, AuditLogSpi {

    private final SecurityContextProvider securityContextProvider;

    /**
     * Creates an audit log repository adapter.
     *
     * @param repository Spring Data audit log repository
     * @param mapper     audit log mapper
     */
    protected AuditLogRepositoryAdapter(AuditLogEntityRepository repository,
            AuditLogMapper mapper,
            SecurityContextProvider securityContextProvider) {
        super(repository, mapper);
        this.securityContextProvider = securityContextProvider;
    }

    /**
     * Persists an audit record in an independent transaction.
     *
     * @param model audit record
     * @return persisted audit record
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog save(AuditLog model) {
        return super.save(model);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action,
            String module,
            String entityType,
            String entityId,
            String description) {
        save(AuditLog.builder()
                .traceId(MDC.get("traceId"))
                .actorId(securityContextProvider.getCurrentUsername())
                .actorIp(securityContextProvider.getCurrentIp())
                .action(action)
                .module(module)
                .resourceType(entityType)
                .resourceId(entityId)
                .description(description)
                .status("SUCCESS")
                .createdAt(Instant.now())
                .build());
    }

    @Override
    protected String resourceType() {
        return "AuditLog";
    }

}
