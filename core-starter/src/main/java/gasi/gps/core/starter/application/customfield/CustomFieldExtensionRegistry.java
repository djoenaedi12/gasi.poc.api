package gasi.gps.core.starter.application.customfield;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import gasi.gps.core.api.application.exception.BusinessException;
import gasi.gps.core.api.customfield.CustomFieldContext;
import gasi.gps.core.api.customfield.CustomFieldExtension;

/**
 * Registry facade for custom field extensions.
 *
 * <p>
 * The starter layer uses this registry so lifecycle hooks can remain
 * custom-field-aware without depending on a concrete custom field plugin.
 * </p>
 *
 * @since 1.0.0
 */
@Component
public class CustomFieldExtensionRegistry {

    private final ObjectProvider<CustomFieldExtension> customFieldExtensions;

    /**
     * Creates a custom field extension registry.
     *
     * @param customFieldExtensions optional custom field extensions
     */
    public CustomFieldExtensionRegistry(ObjectProvider<CustomFieldExtension> customFieldExtensions) {
        this.customFieldExtensions = customFieldExtensions;
    }

    /**
     * Returns whether the first matching custom field extension supports the
     * entity.
     *
     * @param entityCode resource/entity code
     * @return {@code true} when custom fields are supported
     */
    public boolean supports(String entityCode) {
        return firstMatchingExtension(entityCode).isPresent();
    }

    /**
     * Replace custom field values through the first matching custom field
     * extension.
     *
     * @param entityCode resource/entity code
     * @param id         entity identifier
     * @param values     submitted custom field values keyed by field code
     * @param context    custom field validation context
     */
    public void replaceValues(String entityCode, Object id, Map<String, Object> values, CustomFieldContext context) {
        if (values == null || values.isEmpty()) {
            return;
        }

        firstMatchingExtension(entityCode)
                .orElseThrow(() -> new BusinessException("Custom field plugin is not active"))
                .replaceValues(entityCode, id, values, context);
    }

    /**
     * Returns custom field values from the first matching custom field extension.
     *
     * @param entityCode resource/entity code
     * @param id         entity identifier
     * @return custom field values keyed by field code
     */
    public Map<String, Object> getValues(String entityCode, Object id) {
        return firstMatchingExtension(entityCode)
                .map(extension -> extension.getValues(entityCode, id))
                .orElseGet(Map::of);
    }

    /**
     * Delete custom field values through the first matching custom field extension.
     *
     * @param entityCode resource/entity code
     * @param id         entity identifier
     */
    public void deleteValues(String entityCode, Object id) {
        firstMatchingExtension(entityCode)
                .ifPresent(extension -> extension.deleteValues(entityCode, id));
    }

    /**
     * Move custom field values through the first matching custom field extension.
     *
     * @param entityCode resource/entity code
     * @param fromId     source entity identifier
     * @param toId       target entity identifier
     */
    public void moveValues(String entityCode, Object fromId, Object toId) {
        firstMatchingExtension(entityCode)
                .ifPresent(extension -> extension.moveValues(entityCode, fromId, toId));
    }

    private Optional<CustomFieldExtension> firstMatchingExtension(String entityCode) {
        return customFieldExtensions.orderedStream()
                .filter(extension -> extension.supports(entityCode))
                .findFirst();
    }
}
