package gasi.gps.core.api.customfield;

import java.util.Map;

import org.pf4j.ExtensionPoint;

/**
 * PF4J extension point for generic custom field values.
 *
 * <p>
 * The core CRUD service calls this extension point without depending on the
 * custom field plugin implementation. Custom field plugins decide whether an
 * entity supports custom fields, then validate, store, read, delete, or promote
 * custom field values for the affected data.
 * </p>
 *
 * @since 1.0.0
 */
public interface CustomFieldExtension extends ExtensionPoint {

    /**
     * Returns whether the given entity supports custom fields.
     *
     * @param entityCode resource/entity code, usually {@code resourceType()}
     * @return {@code true} when custom fields are supported
     */
    boolean supports(String entityCode);

    /**
     * Replace custom field values for an entity record.
     *
     * <p>
     * Existing custom field values for the entity record are replaced by the
     * submitted values. The plugin implementation is responsible for validating
     * field codes, required rules, data types, options, and other custom field
     * rules.
     * </p>
     *
     * @param entityCode resource/entity code
     * @param id         entity identifier
     * @param values     submitted custom field values keyed by field code
     * @param context    custom field validation context
     */
    void replaceValues(String entityCode, Object id, Map<String, Object> values, CustomFieldContext context);

    /**
     * Returns custom field values for an entity record.
     *
     * @param entityCode resource/entity code
     * @param id         entity identifier
     * @return custom field values keyed by field code
     */
    Map<String, Object> getValues(String entityCode, Object id);

    /**
     * Delete custom field values for an entity record.
     *
     * @param entityCode resource/entity code
     * @param id         entity identifier
     */
    void deleteValues(String entityCode, Object id);

    /**
     * Move custom field values from one entity record to another.
     *
     * <p>
     * This is mainly used when a pending update record is approved and its custom
     * field values must be promoted to the active source record.
     * </p>
     *
     * @param entityCode resource/entity code
     * @param fromId     source entity identifier
     * @param toId       target entity identifier
     */
    void moveValues(String entityCode, Object fromId, Object toId);
}
