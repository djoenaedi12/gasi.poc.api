package gasi.gps.core.api.domain.model;

/**
 * Lifecycle state for domain models that support soft deletion or activation.
 *
 * @since 1.0.0
 */
public enum LifecycleStatus {
    /** Entity is available for normal use. */
    ACTIVE,
    /** Entity is retained but disabled for normal use. */
    INACTIVE,
    /** Entity has been soft-deleted and should be excluded from normal views. */
    DELETED,
    /** Entity is saved but not submitted for approval yet. */
    DRAFT,
    /** Entity creation is waiting for approval. */
    PENDING_CREATE,
    /** Entity update is waiting for approval. */
    PENDING_UPDATE,
    /** Entity deletion is waiting for approval. */
    PENDING_DELETE,
    /** Entity creation or draft submission was rejected. */
    REJECTED
}
