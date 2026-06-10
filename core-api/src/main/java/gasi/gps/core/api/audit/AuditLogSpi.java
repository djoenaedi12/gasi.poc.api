package gasi.gps.core.api.audit;

/**
 * Port for writing audit logs outside of automatic auditing.
 *
 * <p>Use this SPI for explicit business events that are not naturally covered
 * by CRUD interception, such as login, export, approval, or integration
 * callbacks.</p>
 *
 * @since 1.0.0
 */
public interface AuditLogSpi {

    /**
     * Write a manual audit log entry.
     *
     * @param action      the action, for example {@code CREATE}, {@code EXPORT},
     *                    or {@code LOGIN}
     * @param module      module code that owns the event
     * @param entityType  entity type name
     * @param entityId    entity identifier, or {@code null} for non-entity events
     * @param description human-readable description
     */
    void log(String action, String module, String entityType, String entityId, String description);
}
