package gasi.gps.core.starter.application.customfield;

import org.springframework.stereotype.Component;

import gasi.gps.core.api.approval.ApprovalTargetHook;
import gasi.gps.core.api.domain.model.BaseModel;

/**
 * Approval target hook for custom field values.
 *
 * @since 1.0.0
 */
@Component
public class CustomFieldApprovalTargetHook implements ApprovalTargetHook {

    private final CustomFieldExtensionRegistry customFieldExtensionRegistry;

    public CustomFieldApprovalTargetHook(CustomFieldExtensionRegistry customFieldExtensionRegistry) {
        this.customFieldExtensionRegistry = customFieldExtensionRegistry;
    }

    @Override
    public void afterCreateApproved(String resourceType, BaseModel saved) {
        // No operation. Pending create keeps the same id and becomes active.
    }

    @Override
    public void afterUpdateApproved(String resourceType, BaseModel saved, BaseModel pending) {
        customFieldExtensionRegistry.moveValues(resourceType, pending.getId(), saved.getId());
    }

    @Override
    public void afterDeleteApproved(String resourceType, BaseModel active) {
        customFieldExtensionRegistry.deleteValues(resourceType, active.getId());
    }

    @Override
    public void afterCreateRejected(String resourceType, BaseModel pending) {
        customFieldExtensionRegistry.deleteValues(resourceType, pending.getId());
    }

    @Override
    public void afterUpdateRejected(String resourceType, BaseModel pending) {
        customFieldExtensionRegistry.deleteValues(resourceType, pending.getId());
    }

    @Override
    public void afterDeleteRejected(String resourceType, BaseModel saved) {
        // No operation. Active custom field values remain unchanged.
    }

    @Override
    public String resourceType() {
        return "";
    }
}
