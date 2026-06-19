package gasi.gps.core.api.approval;

import org.pf4j.ExtensionPoint;

/**
 * PF4J extension point for generic approval workflows.
 *
 * <p>
 * The core CRUD service calls this extension point without depending on the
 * approval plugin implementation. Approval plugins decide whether an entity and
 * action require approval, then submit workflow requests with snapshots of the
 * affected data.
 * </p>
 *
 * @since 1.0.0
 */
public interface ApprovalExtension extends ExtensionPoint {

    /**
     * Returns whether the given entity action requires approval.
     *
     * @param entityCode resource/entity code, usually {@code resourceType()}
     * @param action     requested mutation action
     * @return {@code true} when approval is required
     */
    boolean isRequired(String entityCode, ApprovalAction action);

    /**
     * Submit a create approval request after the pending entity has been saved.
     *
     * @param entityCode resource/entity code
     * @param id         saved entity identifier
     * @param newData    saved pending entity data
     * @return approval submission result
     */
    ApprovalResult submitCreate(String entityCode, Object id, Object newData);

    /**
     * Submit an update approval request.
     *
     * @param entityCode resource/entity code
     * @param id         entity identifier
     * @param oldData    current approved data
     * @param newData    proposed data
     * @return approval submission result
     */
    ApprovalResult submitUpdate(String entityCode, Object id, Object oldData, Object newData);

    /**
     * Submit a delete approval request.
     *
     * @param entityCode resource/entity code
     * @param id         entity identifier
     * @param oldData    current approved data
     * @return approval submission result
     */
    ApprovalResult submitDelete(String entityCode, Object id, Object oldData);
}
