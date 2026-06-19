package gasi.gps.core.api.approval;

/**
 * Result returned by an approval extension after an approval request is
 * submitted.
 *
 * @param submitted         whether an approval request was submitted
 * @param approvalRequestId plugin-specific approval request identifier
 * @param message           optional human-readable message
 * @since 1.0.0
 */
public record ApprovalResult(
        boolean submitted,
        Object approvalRequestId,
        String message) {

    /**
     * Returns a result for operations that do not require approval.
     *
     * @return no-approval result
     */
    public static ApprovalResult notRequired() {
        return new ApprovalResult(false, null, null);
    }

    /**
     * Returns a result for submitted approval requests.
     *
     * @param approvalRequestId plugin-specific approval request identifier
     * @return submitted result
     */
    public static ApprovalResult submitted(Object approvalRequestId) {
        return new ApprovalResult(true, approvalRequestId, null);
    }
}
