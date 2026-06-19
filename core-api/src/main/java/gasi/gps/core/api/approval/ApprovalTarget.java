package gasi.gps.core.api.approval;

/**
 * Generic approval target for a resource that can be completed by an approval
 * workflow.
 *
 * <p>
 * This contract is implemented by resource services that can execute the
 * final result of an approval request. The approval workflow engine should use
 * this target only after an approval request reaches its final approved or
 * rejected state.
 * </p>
 *
 * @since 1.0.0
 */
public interface ApprovalTarget {

    /**
     * Returns the resource type handled by this target.
     *
     * @return resource type
     */
    String resourceType();

    /**
     * Applies a final approved create request.
     *
     * <p>
     * For generated CRUD resources, this usually activates a pending create
     * record.
     * </p>
     *
     * @param id internal database identifier
     */
    void approveCreate(Long id);

    /**
     * Applies a final approved update request.
     *
     * <p>
     * For generated CRUD resources, this usually copies pending update data
     * into the active record and removes the pending record.
     * </p>
     *
     * @param id internal database identifier
     */
    void approveUpdate(Long id);

    /**
     * Applies a final approved delete request.
     *
     * <p>
     * For generated CRUD resources, this usually performs the actual hard
     * delete after approval is completed.
     * </p>
     *
     * @param id internal database identifier
     */
    void approveDelete(Long id);

    /**
     * Applies a final rejected create request.
     *
     * <p>
     * For generated CRUD resources, this usually removes the pending create
     * record.
     * </p>
     *
     * @param id internal database identifier
     */
    void rejectCreate(Long id);

    /**
     * Applies a final rejected update request.
     *
     * <p>
     * For generated CRUD resources, this usually removes the pending update
     * record and keeps the active record unchanged.
     * </p>
     *
     * @param id internal database identifier
     */
    void rejectUpdate(Long id);

    /**
     * Applies a final rejected delete request.
     *
     * <p>
     * For generated CRUD resources, this usually restores the record from a
     * pending delete state back to its active state.
     * </p>
     *
     * @param id internal database identifier
     */
    void rejectDelete(Long id);
}
