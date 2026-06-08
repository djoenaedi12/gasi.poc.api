package gasi.gps.dataupload.domain.model;

/**
 * Processing status of a single uploaded row.
 *
 * @since 1.0.0
 */
public enum UploadRowStatus {
    /** Row has been read but not validated. */
    RAW,
    /** Row passed validation. */
    VALID,
    /** Row failed validation. */
    INVALID,
    /** Row has been committed to the target resource. */
    COMMITTED,
}
