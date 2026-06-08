package gasi.gps.audit.infrastructure.persistence;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import gasi.gps.audit.infrastructure.entity.AuditLogEntity;

/**
 * Spring Data repository for audit log entities.
 *
 * @since 1.0.0
 */
public interface AuditLogEntityRepository
        extends JpaRepository<AuditLogEntity, Long>, JpaSpecificationExecutor<AuditLogEntity> {
}
