package gasi.gps.core.api.domain.port.inbound;

/**
 * Options for write operations.
 *
 * @param bypassApproval whether approval checks should be skipped
 * @since 1.0.0
 */
public record MutationOptions(boolean bypassApproval) {

    /**
     * Returns default mutation options.
     *
     * @return default options
     */
    public static MutationOptions defaults() {
        return new MutationOptions(false);
    }

    /**
     * Returns options for an already-approved mutation.
     *
     * @return approval-bypass options
     */
    public static MutationOptions approvalBypassed() {
        return new MutationOptions(true);
    }
}
