package gasi.gps.core.api.application.hook;

/**
 * Resource hook execution layer.
 *
 * @since 1.0.0
 */
public enum HookLayer {
    /** Service-layer lifecycle hooks. */
    SERVICE,

    /** Controller-layer lifecycle hooks. */
    CONTROLLER,

    /** Repository-layer lifecycle hooks. */
    REPOSITORY,

    /** Mapper-layer lifecycle hooks. */
    MAPPER,

    /** Data upload lifecycle hooks. */
    UPLOADER
}
