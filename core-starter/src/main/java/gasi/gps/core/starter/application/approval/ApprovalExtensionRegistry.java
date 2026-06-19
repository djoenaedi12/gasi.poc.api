package gasi.gps.core.starter.application.approval;

import java.util.Optional;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import gasi.gps.core.api.approval.ApprovalAction;
import gasi.gps.core.api.approval.ApprovalExtension;
import gasi.gps.core.api.approval.ApprovalResult;

/**
 * Registry facade for approval extensions.
 *
 * <p>
 * The starter layer uses this registry so base CRUD services can remain
 * approval-aware without depending on a concrete approval plugin.
 * </p>
 *
 * @since 1.0.0
 */
@Component
public class ApprovalExtensionRegistry {

    private final ObjectProvider<ApprovalExtension> approvalExtensions;

    /**
     * Creates an approval extension registry.
     *
     * @param approvalExtensions optional approval extensions
     */
    public ApprovalExtensionRegistry(ObjectProvider<ApprovalExtension> approvalExtensions) {
        this.approvalExtensions = approvalExtensions;
    }

    /**
     * Returns whether the first approval extension requires approval for the
     * action.
     *
     * @param entityCode resource/entity code
     * @param action     requested mutation action
     * @return {@code true} when approval is required
     */
    public boolean isRequired(String entityCode, ApprovalAction action) {
        return firstExtension()
                .map(extension -> extension.isRequired(entityCode, action))
                .orElse(false);
    }

    /**
     * Submit a create approval request to the first approval extension.
     *
     * @param entityCode resource/entity code
     * @param id         saved entity identifier
     * @param newData    saved pending entity data
     * @return approval submission result
     */
    public ApprovalResult submitCreate(String entityCode, Object id, Object newData) {
        return firstExtension()
                .map(extension -> extension.submitCreate(entityCode, id, newData))
                .orElseGet(ApprovalResult::notRequired);
    }

    /**
     * Submit an update approval request to the first approval extension.
     *
     * @param entityCode resource/entity code
     * @param id         current entity identifier
     * @param oldData    current active entity data
     * @param newData    pending updated entity data
     * @return approval submission result
     */
    public ApprovalResult submitUpdate(String entityCode, Object id, Object oldData, Object newData) {
        return firstExtension()
                .map(extension -> extension.submitUpdate(entityCode, id, oldData, newData))
                .orElseGet(ApprovalResult::notRequired);
    }

    /**
     * Submit a delete approval request to the first approval extension.
     *
     * @param entityCode resource/entity code
     * @param id         entity identifier
     * @param oldData    current entity data
     * @return approval submission result
     */
    public ApprovalResult submitDelete(String entityCode, Object id, Object oldData) {
        return firstExtension()
                .map(extension -> extension.submitDelete(entityCode, id, oldData))
                .orElseGet(ApprovalResult::notRequired);
    }

    private Optional<ApprovalExtension> firstExtension() {
        return approvalExtensions.orderedStream().findFirst();
    }
}
