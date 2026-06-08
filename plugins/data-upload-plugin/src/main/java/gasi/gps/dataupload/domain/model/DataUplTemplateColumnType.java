package gasi.gps.dataupload.domain.model;

/**
 * Supported logical data types for upload template columns.
 *
 * @since 1.0.0
 */
public enum DataUplTemplateColumnType {
    /** Free text value. */
    TEXT,
    /** Numeric value. */
    NUMBER,
    /** Date value. */
    DATE,
    /** Boolean value. */
    BOOLEAN,
    /** Static option list value. */
    ENUM,
    /** Dynamic lookup/reference value. */
    LOOKUP
}
