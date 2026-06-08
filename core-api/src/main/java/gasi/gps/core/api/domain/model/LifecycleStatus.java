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
    DELETED
}
