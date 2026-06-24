package gasi.gps.core.api.approval;

import org.pf4j.ExtensionPoint;

import gasi.gps.core.api.domain.model.BaseModel;

/**
 * Hook invoked after an approval target execution has been applied to target
 * data.
 *
 * <p>
 * This hook is intended for custom post-approval side effects such as
 * notifications, synchronization, cache refresh, or audit enrichment.
 * </p>
 *
 * @since 1.0.0
 */
public interface ApprovalTargetHook extends ExtensionPoint {

    /**
     * Returns the resource type handled by this hook.
     *
     * @return resource type
     */
    String resourceType();

    /**
     * Called after a pending create request is approved and the pending data is
     * activated.
     *
     * @param resourceType resource type
     * @param data         approved and activated data
     */
    default void afterCreateApproved(String resourceType, BaseModel data) {
    }

    /**
     * Called after a pending create request is rejected and the pending data is
     * removed.
     *
     * @param resourceType resource type
     * @param data         rejected pending data
     */
    default void afterCreateRejected(String resourceType, BaseModel data) {
    }

    /**
     * Called after a pending update request is approved and applied to the
     * active data.
     *
     * @param resourceType resource type
     * @param activeData   active data after the approved change is saved
     * @param pendingData  pending data used as the approved change source
     */
    default void afterUpdateApproved(String resourceType, BaseModel activeData, BaseModel pendingData) {
    }

    /**
     * Called after a pending update request is rejected and the pending data is
     * removed.
     *
     * @param resourceType resource type
     * @param pendingData  rejected pending update data
     */
    default void afterUpdateRejected(String resourceType, BaseModel pendingData) {
    }

    /**
     * Called after a pending delete request is approved and the target data is
     * deleted.
     *
     * @param resourceType resource type
     * @param data         deleted data
     */
    default void afterDeleteApproved(String resourceType, BaseModel data) {
    }

    /**
     * Called after a pending delete request is rejected and the target data
     * remains active.
     *
     * @param resourceType resource type
     * @param data         active data restored from pending delete flow
     */
    default void afterDeleteRejected(String resourceType, BaseModel data) {
    }
}
