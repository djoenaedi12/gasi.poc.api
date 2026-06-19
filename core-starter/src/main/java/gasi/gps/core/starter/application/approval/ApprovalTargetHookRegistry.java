package gasi.gps.core.starter.application.approval;

import java.util.List;

import org.springframework.stereotype.Component;

import gasi.gps.core.api.approval.ApprovalTargetHook;
import gasi.gps.core.api.domain.model.BaseModel;

/**
 * Registry facade for approval target hooks.
 *
 * <p>
 * This registry invokes hooks contributed by resource modules after an approval
 * target execution has been applied to target data.
 * </p>
 *
 * @since 1.0.0
 */
@Component
public class ApprovalTargetHookRegistry {

    private final List<ApprovalTargetHook> hooks;

    /**
     * Creates an approval target hook registry.
     *
     * @param hooks approval target hooks
     */
    public ApprovalTargetHookRegistry(List<ApprovalTargetHook> hooks) {
        this.hooks = List.copyOf(hooks);
    }

    /**
     * Invokes create-approved hooks for a resource.
     *
     * @param resourceType resource type
     * @param data         approved and activated data
     */
    public void afterCreateApproved(String resourceType, BaseModel data) {
        matchingHooks(resourceType).forEach(hook -> hook.afterCreateApproved(data));
    }

    /**
     * Invokes create-rejected hooks for a resource.
     *
     * @param resourceType resource type
     * @param data         rejected pending data
     */
    public void afterCreateRejected(String resourceType, BaseModel data) {
        matchingHooks(resourceType).forEach(hook -> hook.afterCreateRejected(data));
    }

    /**
     * Invokes update-approved hooks for a resource.
     *
     * @param resourceType resource type
     * @param activeData   active data after the approved change is saved
     * @param pendingData  pending data used as the approved change source
     */
    public void afterUpdateApproved(String resourceType, BaseModel activeData, BaseModel pendingData) {
        matchingHooks(resourceType).forEach(hook -> hook.afterUpdateApproved(activeData, pendingData));
    }

    /**
     * Invokes update-rejected hooks for a resource.
     *
     * @param resourceType resource type
     * @param activeData   active data that remains unchanged
     * @param pendingData  rejected pending update data
     */
    public void afterUpdateRejected(String resourceType, BaseModel pendingData) {
        matchingHooks(resourceType).forEach(hook -> hook.afterUpdateRejected(pendingData));
    }

    /**
     * Invokes delete-approved hooks for a resource.
     *
     * @param resourceType resource type
     * @param data         deleted data
     */
    public void afterDeleteApproved(String resourceType, BaseModel data) {
        matchingHooks(resourceType).forEach(hook -> hook.afterDeleteApproved(data));
    }

    /**
     * Invokes delete-rejected hooks for a resource.
     *
     * @param resourceType resource type
     * @param data         active data restored from pending delete flow
     */
    public void afterDeleteRejected(String resourceType, BaseModel data) {
        matchingHooks(resourceType).forEach(hook -> hook.afterDeleteRejected(data));
    }

    private List<ApprovalTargetHook> matchingHooks(String resourceType) {
        return hooks.stream()
                .filter(hook -> resourceType.equals(hook.resourceType()))
                .toList();
    }
}
