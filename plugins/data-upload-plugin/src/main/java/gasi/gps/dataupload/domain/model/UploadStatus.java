package gasi.gps.dataupload.domain.model;

/**
 * Lifecycle status of an upload batch.
 *
 * @since 1.0.0
 */
public enum UploadStatus {
    /** File upload is in progress. */
    UPLOADING,
    /** File has been uploaded and rows are available for processing. */
    UPLOADED,
    /** Uploaded rows are being validated. */
    VALIDATING,
    /** Uploaded rows have been validated. */
    VALIDATED,
    /** Valid rows are being committed to the target resource. */
    COMMITTING,
    /** Upload is waiting for approval before commit. */
    PENDING_APPROVAL,
    /** Upload has been committed successfully. */
    COMMITTED,
    /** Upload was rejected before commit. */
    REJECTED,
    /** Upload processing failed. */
    FAILED,
}
