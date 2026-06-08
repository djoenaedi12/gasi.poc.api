package gasi.gps.core.api.application.dto;

/**
 * Request contract for DTOs that carry an optimistic locking version.
 *
 * <p>Update operations can implement this interface to expose the version
 * supplied by the client and detect stale writes.</p>
 *
 * @since 1.0.0
 */
public interface VersionedRequest {

    /**
     * Returns the optimistic locking version supplied by the client.
     *
     * @return client-provided version, or {@code null} when the request does
     *         not include one
     */
    Integer getVersion();
}
