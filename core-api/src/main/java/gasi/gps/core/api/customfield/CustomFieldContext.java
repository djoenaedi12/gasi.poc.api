package gasi.gps.core.api.customfield;

import java.util.Collections;
import java.util.Map;

/**
 * Context used to validate custom field rules.
 *
 * <p>
 * The context contains core entity field values and submitted custom field
 * values, so custom field plugins can evaluate rules without depending on a
 * concrete entity class.
 * </p>
 *
 * @since 1.0.0
 */
public class CustomFieldContext {

    private final Map<String, Object> entityFields;

    private final Map<String, Object> customFields;

    /**
     * Creates a custom field validation context.
     *
     * @param entityFields core entity field values
     * @param customFields submitted custom field values
     */
    public CustomFieldContext(Map<String, Object> entityFields, Map<String, Object> customFields) {
        this.entityFields = entityFields == null ? Collections.emptyMap() : Map.copyOf(entityFields);
        this.customFields = customFields == null ? Collections.emptyMap() : Map.copyOf(customFields);
    }

    /**
     * Returns core entity field values.
     *
     * @return core entity field values
     */
    public Map<String, Object> getEntityFields() {
        return entityFields;
    }

    /**
     * Returns submitted custom field values.
     *
     * @return submitted custom field values
     */
    public Map<String, Object> getCustomFields() {
        return customFields;
    }

    /**
     * Returns a value by source type and field code.
     *
     * @param sourceType source type, usually {@code ENTITY_FIELD} or
     *                   {@code CUSTOM_FIELD}
     * @param fieldCode  field code
     * @return field value, or {@code null} when not found
     */
    public Object getValue(String sourceType, String fieldCode) {
        if ("ENTITY_FIELD".equals(sourceType)) {
            return entityFields.get(fieldCode);
        }
        if ("CUSTOM_FIELD".equals(sourceType)) {
            return customFields.get(fieldCode);
        }
        return null;
    }
}
