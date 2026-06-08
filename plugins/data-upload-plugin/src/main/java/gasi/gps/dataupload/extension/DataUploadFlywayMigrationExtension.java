package gasi.gps.dataupload.extension;

import org.pf4j.Extension;
import gasi.gps.core.api.migration.FlywayMigrationExtension;
import lombok.NoArgsConstructor;

/**
 * Registers the Flyway migration location for plugin data-upload-plugin
 * so that it is included by the core application during startup.
 *
 * <p>Migration location: {@code classpath:db/migration/data-upload}.</p>
 *
 * @since 1.0.0
 */
@Extension
@NoArgsConstructor
public class DataUploadFlywayMigrationExtension implements FlywayMigrationExtension {

    @Override
    public String getMigrationLocation() {
        return "classpath:db/migration/data-upload";
    }
}
