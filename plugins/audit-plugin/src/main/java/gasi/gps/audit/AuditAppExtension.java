package gasi.gps.audit;

import java.util.List;

import org.pf4j.Extension;

import gasi.gps.core.api.extension.AppExtension;
import lombok.NoArgsConstructor;

/**
 * PF4J extension providing audit plugin metadata to the core application.
 *
 * @since 1.0.0
 */
@Extension
@NoArgsConstructor
public class AuditAppExtension implements AppExtension {

    @Override
    public String getModuleName() {
        return "audit-plugin";
    }

    @Override
    public String getModuleDescription() {
        return "Provides automatic audit logging for CUD operations and custom method-level auditing.";
    }

    @Override
    public String getModuleVersion() {
        return "1.0.0";
    }

    @Override
    public List<String> getBasePackages() {
        return List.of("gasi.gps.audit");
    }
}
