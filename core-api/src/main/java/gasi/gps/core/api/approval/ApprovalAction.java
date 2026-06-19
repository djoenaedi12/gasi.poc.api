package gasi.gps.core.api.approval;

/**
 * Standard actions that may require approval.
 *
 * @since 1.0.0
 */
public enum ApprovalAction {
    /** Create a new business record. */
    CREATE,
    /** Update an existing business record. */
    UPDATE,
    /** Delete an existing business record. */
    DELETE
}
