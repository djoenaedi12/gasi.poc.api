package gasi.gps.core.api.domain.port.inbound;

import gasi.gps.core.api.domain.model.BaseModel;

/**
 * Inbound service contract for standard CRUD resource operations.
 *
 * <p>This interface is intentionally framework-neutral. Implementations may
 * live in plugin modules or in reusable starter support, while callers depend
 * only on this API-level contract. It extends {@link BaseReadService} with
 * the standard create, update, and delete commands.</p>
 *
 * @param <D>   domain model type
 * @param <CRQ> create request DTO type
 * @param <URQ> update request DTO type
 * @param <SRS> summary response DTO type
 * @param <DRS> detail response DTO type
 * @since 1.0.0
 */
public interface BaseService<D extends BaseModel, CRQ, URQ, SRS, DRS>
        extends BaseReadService<SRS, DRS> {

    /**
     * Creates a new resource from the given request.
     *
     * @param request create request payload
     * @return detail response for the created resource
     */
    default DRS create(CRQ request) {
        return create(request, MutationOptions.defaults());
    }

    /**
     * Creates a new resource from the given request.
     *
     * @param request create request payload
     * @param options mutation options
     * @return detail response for the created resource
     */
    DRS create(CRQ request, MutationOptions options);

    /**
     * Updates an existing resource.
     *
     * @param id      internal database identifier
     * @param request update request payload
     * @return detail response for the updated resource
     */
    default DRS update(Long id, URQ request) {
        return update(id, request, MutationOptions.defaults());
    }

    /**
     * Updates an existing resource.
     *
     * @param id      internal database identifier
     * @param request update request payload
     * @param options mutation options
     * @return detail response for the updated resource
     */
    DRS update(Long id, URQ request, MutationOptions options);

    /**
     * Deletes a resource by internal numeric identifier.
     *
     * @param id internal database identifier
     */
    default void delete(Long id) {
        delete(id, MutationOptions.defaults());
    }

    /**
     * Deletes a resource by internal numeric identifier.
     *
     * @param id      internal database identifier
     * @param options mutation options
     */
    void delete(Long id, MutationOptions options);
}
