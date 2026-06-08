package gasi.gps.audit;

import org.pf4j.Extension;

import gasi.gps.core.api.migration.FlywayMigrationExtension;
import lombok.NoArgsConstructor;

/**
 * PF4J extension that registers the audit plugin's Flyway migration location.
 *
 * @since 1.0.0
 */
@Extension
@NoArgsConstructor
public class AuditFlywayExtension implements FlywayMigrationExtension {

    @Override
    public String getMigrationLocation() {
        return "classpath:db/migration";
    }
}
