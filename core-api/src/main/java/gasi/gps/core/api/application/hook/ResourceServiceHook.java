package gasi.gps.core.api.application.hook;

import java.util.ArrayList;
import java.util.List;

import gasi.gps.core.api.domain.model.BaseModel;
import gasi.gps.core.api.domain.model.GenericFilter;
import gasi.gps.core.api.domain.model.PageResult;
import gasi.gps.core.api.domain.model.SortOrder;

/**
 * Lifecycle hook contract for resource service operations.
 *
 * <p>
 * Implementations may be generated from a resource JSON specification or added
 * manually for resource-specific business rules. All methods are no-op by
 * default.
 * </p>
 *
 * @param <D>   domain model type
 * @param <CRQ> create request DTO type
 * @param <URQ> update request DTO type
 * @param <SRS> summary response DTO type
 * @param <DRS> detail response DTO type
 * @since 1.0.0
 */
public interface ResourceServiceHook<D extends BaseModel, CRQ, URQ, SRS, DRS> {

    /**
     * Called at the beginning of find-by-id.
     *
     * @param id internal database identifier
     */
    default void beforeFindById(Long id) {
    }

    /**
     * Called after find-by-id has been mapped and enriched.
     *
     * @param response detail response that will be returned
     * @param domain   loaded domain object
     */
    default void afterFindByIdResponse(DRS response, D domain) {
    }

    /**
     * Called at the beginning of find-by-filter.
     *
     * @param filter filter expression
     */
    default void beforeFindBy(GenericFilter filter) {
    }

    /**
     * Called after find-by-filter has been mapped and enriched.
     *
     * @param response detail response that will be returned
     * @param domain   loaded domain object
     */
    default void afterFindByResponse(DRS response, D domain) {
    }

    /**
     * Called at the beginning of find-all.
     *
     * @param filter filter expression
     * @param orders sort orders
     */
    default void beforeFindAll(GenericFilter filter, List<SortOrder> orders) {
    }

    /**
     * Called after find-all has been mapped.
     *
     * @param response summary responses that will be returned
     * @param domains  loaded domain objects
     */
    default void afterFindAllResponse(List<SRS> response, List<D> domains) {
    }

    /**
     * Called at the beginning of paged find-all.
     *
     * @param page   zero-based page index
     * @param size   requested page size
     * @param filter filter expression
     * @param orders sort orders
     */
    default void beforeFindAllPaged(int page, int size, GenericFilter filter, List<SortOrder> orders) {
    }

    /**
     * Called after paged find-all has been mapped.
     *
     * @param response page response that will be returned
     * @param domains  loaded domain page
     */
    default void afterFindAllPagedResponse(PageResult<SRS> response, PageResult<D> domains) {
    }

    /**
     * Called at the beginning of create, before request-to-domain mapping.
     *
     * @param request create request payload
     */
    default void beforeCreateRequest(CRQ request) {
    }

    /**
     * Called after create DTO is mapped to domain, before save.
     *
     * @param domain  newly mapped domain object
     * @param request create request payload
     */
    default void beforeCreate(D domain, CRQ request) {
    }

    /**
     * Called after the entity is saved on create.
     *
     * @param saved   persisted domain object
     * @param request create request payload
     */
    default void afterCreate(D saved, CRQ request) {
    }

    /**
     * Called after create has been saved and mapped to a detail response.
     *
     * @param response detail response that will be returned
     * @param saved    persisted domain object
     * @param request  create request payload
     */
    default void afterCreateResponse(DRS response, D saved, CRQ request) {
    }

    /**
     * Called at the beginning of update, before loading and mapping the entity.
     *
     * @param id      internal database identifier
     * @param request update request payload
     */
    default void beforeUpdateRequest(Long id, URQ request) {
    }

    /**
     * Called after update DTO is merged into existing domain, before save.
     *
     * @param domain  existing domain object with updates applied
     * @param request update request payload
     */
    default void beforeUpdate(D domain, URQ request) {
    }

    /**
     * Called after the entity is saved on update.
     *
     * @param saved   persisted domain object
     * @param request update request payload
     */
    default void afterUpdate(D saved, URQ request) {
    }

    /**
     * Called after update has been saved and mapped to a detail response.
     *
     * @param response detail response that will be returned
     * @param saved    persisted domain object
     * @param request  update request payload
     */
    default void afterUpdateResponse(DRS response, D saved, URQ request) {
    }

    /**
     * Called at the beginning of delete, before loading the entity.
     *
     * @param id internal database identifier
     */
    default void beforeDeleteRequest(Long id) {
    }

    /**
     * Called before the entity is deleted.
     *
     * @param id internal database identifier
     */
    default void beforeDelete(Long id) {
    }

    /**
     * Called after the entity is deleted.
     *
     * @param id internal database identifier
     */
    default void afterDelete(Long id) {
    }

    /**
     * Returns a no-op hook.
     *
     * @param <D>   domain model type
     * @param <CRQ> create request DTO type
     * @param <URQ> update request DTO type
     * @param <SRS> summary response DTO type
     * @param <DRS> detail response DTO type
     * @return no-op hook
     */
    static <D extends BaseModel, CRQ, URQ, SRS, DRS> ResourceServiceHook<D, CRQ, URQ, SRS, DRS> noop() {
        return new ResourceServiceHook<>() {
        };
    }

    /**
     * Composes hooks in the provided order.
     *
     * @param hooks hooks to execute
     * @param <D>   domain model type
     * @param <CRQ> create request DTO type
     * @param <URQ> update request DTO type
     * @param <SRS> summary response DTO type
     * @param <DRS> detail response DTO type
     * @return composite hook
     */
    static <D extends BaseModel, CRQ, URQ, SRS, DRS> ResourceServiceHook<D, CRQ, URQ, SRS, DRS> composite(
            List<? extends ResourceServiceHook<D, CRQ, URQ, SRS, DRS>> hooks) {
        List<ResourceServiceHook<D, CRQ, URQ, SRS, DRS>> delegates = new ArrayList<>();
        if (hooks != null) {
            for (ResourceServiceHook<D, CRQ, URQ, SRS, DRS> hook : hooks) {
                if (hook != null) {
                    delegates.add(hook);
                }
            }
        }
        if (delegates.isEmpty()) {
            return noop();
        }
        if (delegates.size() == 1) {
            return delegates.get(0);
        }
        return new ResourceServiceHook<>() {
            @Override
            public void beforeFindById(Long id) {
                delegates.forEach(hook -> hook.beforeFindById(id));
            }

            @Override
            public void afterFindByIdResponse(DRS response, D domain) {
                delegates.forEach(hook -> hook.afterFindByIdResponse(response, domain));
            }

            @Override
            public void beforeFindBy(GenericFilter filter) {
                delegates.forEach(hook -> hook.beforeFindBy(filter));
            }

            @Override
            public void afterFindByResponse(DRS response, D domain) {
                delegates.forEach(hook -> hook.afterFindByResponse(response, domain));
            }

            @Override
            public void beforeFindAll(GenericFilter filter, List<SortOrder> orders) {
                delegates.forEach(hook -> hook.beforeFindAll(filter, orders));
            }

            @Override
            public void afterFindAllResponse(List<SRS> response, List<D> domains) {
                delegates.forEach(hook -> hook.afterFindAllResponse(response, domains));
            }

            @Override
            public void beforeFindAllPaged(int page, int size, GenericFilter filter, List<SortOrder> orders) {
                delegates.forEach(hook -> hook.beforeFindAllPaged(page, size, filter, orders));
            }

            @Override
            public void afterFindAllPagedResponse(PageResult<SRS> response, PageResult<D> domains) {
                delegates.forEach(hook -> hook.afterFindAllPagedResponse(response, domains));
            }

            @Override
            public void beforeCreateRequest(CRQ request) {
                delegates.forEach(hook -> hook.beforeCreateRequest(request));
            }

            @Override
            public void beforeCreate(D domain, CRQ request) {
                delegates.forEach(hook -> hook.beforeCreate(domain, request));
            }

            @Override
            public void afterCreate(D saved, CRQ request) {
                delegates.forEach(hook -> hook.afterCreate(saved, request));
            }

            @Override
            public void afterCreateResponse(DRS response, D saved, CRQ request) {
                delegates.forEach(hook -> hook.afterCreateResponse(response, saved, request));
            }

            @Override
            public void beforeUpdateRequest(Long id, URQ request) {
                delegates.forEach(hook -> hook.beforeUpdateRequest(id, request));
            }

            @Override
            public void beforeUpdate(D domain, URQ request) {
                delegates.forEach(hook -> hook.beforeUpdate(domain, request));
            }

            @Override
            public void afterUpdate(D saved, URQ request) {
                delegates.forEach(hook -> hook.afterUpdate(saved, request));
            }

            @Override
            public void afterUpdateResponse(DRS response, D saved, URQ request) {
                delegates.forEach(hook -> hook.afterUpdateResponse(response, saved, request));
            }

            @Override
            public void beforeDeleteRequest(Long id) {
                delegates.forEach(hook -> hook.beforeDeleteRequest(id));
            }

            @Override
            public void beforeDelete(Long id) {
                delegates.forEach(hook -> hook.beforeDelete(id));
            }

            @Override
            public void afterDelete(Long id) {
                delegates.forEach(hook -> hook.afterDelete(id));
            }
        };
    }
}
