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
     * @param resourceType resource type, usually {@code resourceType()}
     * @param id           internal database identifier
     */
    default void beforeFindById(String resourceType, Long id) {
    }

    /**
     * Called after find-by-id has been mapped and enriched.
     *
     * @param resourceType resource type, usually {@code resourceType()}
     * @param response     detail response that will be returned
     * @param domain       loaded domain object
     */
    default void afterFindByIdResponse(String resourceType, DRS response, D domain) {
    }

    /**
     * Called at the beginning of find-by-filter.
     *
     * @param resourceType resource type, usually {@code resourceType()}
     * @param filter       filter expression
     */
    default void beforeFindBy(String resourceType, GenericFilter filter) {
    }

    /**
     * Called after find-by-filter has been mapped and enriched.
     *
     * @param resourceType resource type, usually {@code resourceType()}
     * @param response     detail response that will be returned
     * @param domain       loaded domain object
     */
    default void afterFindByResponse(String resourceType, DRS response, D domain) {
    }

    /**
     * Called at the beginning of find-all.
     *
     * @param resourceType resource type, usually {@code resourceType()}
     * @param filter       filter expression
     * @param orders       sort orders
     */
    default void beforeFindAll(String resourceType, GenericFilter filter, List<SortOrder> orders) {
    }

    /**
     * Called after find-all has been mapped.
     *
     * @param resourceType resource type, usually {@code resourceType()}
     * @param response     summary responses that will be returned
     * @param domains      loaded domain objects
     */
    default void afterFindAllResponse(String resourceType, List<SRS> response, List<D> domains) {
    }

    /**
     * Called at the beginning of paged find-all.
     *
     * @param resourceType resource type, usually {@code resourceType()}
     * @param page         zero-based page index
     * @param size         requested page size
     * @param filter       filter expression
     * @param orders       sort orders
     */
    default void beforeFindAllPaged(
            String resourceType,
            int page,
            int size,
            GenericFilter filter,
            List<SortOrder> orders) {
    }

    /**
     * Called after paged find-all has been mapped.
     *
     * @param resourceType resource type, usually {@code resourceType()}
     * @param response     page response that will be returned
     * @param domains      loaded domain page
     */
    default void afterFindAllPagedResponse(String resourceType, PageResult<SRS> response, PageResult<D> domains) {
    }

    /**
     * Called at the beginning of create, before request-to-domain mapping.
     *
     * @param resourceType resource type, usually {@code resourceType()}
     * @param request      create request payload
     */
    default void beforeCreateRequest(String resourceType, CRQ request) {
    }

    /**
     * Called after create DTO is mapped to domain, before save.
     *
     * @param resourceType resource type, usually {@code resourceType()}
     * @param domain       newly mapped domain object
     * @param request      create request payload
     */
    default void beforeCreate(String resourceType, D domain, CRQ request) {
    }

    /**
     * Called after the entity is saved on create.
     *
     * @param resourceType resource type, usually {@code resourceType()}
     * @param saved        persisted domain object
     * @param request      create request payload
     */
    default void afterCreate(String resourceType, D saved, CRQ request) {
    }

    /**
     * Called after create has been saved and mapped to a detail response.
     *
     * @param resourceType resource type, usually {@code resourceType()}
     * @param response     detail response that will be returned
     * @param saved        persisted domain object
     * @param request      create request payload
     */
    default void afterCreateResponse(String resourceType, DRS response, D saved, CRQ request) {
    }

    /**
     * Called at the beginning of update, before loading and mapping the entity.
     *
     * @param resourceType resource type, usually {@code resourceType()}
     * @param id           internal database identifier
     * @param request      update request payload
     */
    default void beforeUpdateRequest(String resourceType, Long id, URQ request) {
    }

    /**
     * Called after update DTO is merged into existing domain, before save.
     *
     * @param resourceType resource type, usually {@code resourceType()}
     * @param domain       existing domain object with updates applied
     * @param request      update request payload
     */
    default void beforeUpdate(String resourceType, D domain, URQ request) {
    }

    /**
     * Called after the entity is saved on update.
     *
     * @param resourceType resource type, usually {@code resourceType()}
     * @param saved        persisted domain object
     * @param request      update request payload
     */
    default void afterUpdate(String resourceType, D saved, URQ request) {
    }

    /**
     * Called after update has been saved and mapped to a detail response.
     *
     * @param resourceType resource type, usually {@code resourceType()}
     * @param response     detail response that will be returned
     * @param saved        persisted domain object
     * @param request      update request payload
     */
    default void afterUpdateResponse(String resourceType, DRS response, D saved, URQ request) {
    }

    /**
     * Called at the beginning of delete, before loading the entity.
     *
     * @param resourceType resource type, usually {@code resourceType()}
     * @param id           internal database identifier
     */
    default void beforeDeleteRequest(String resourceType, Long id) {
    }

    /**
     * Called before the entity is deleted.
     *
     * @param resourceType resource type, usually {@code resourceType()}
     * @param id           internal database identifier
     */
    default void beforeDelete(String resourceType, Long id) {
    }

    /**
     * Called after the entity is deleted.
     *
     * @param resourceType resource type, usually {@code resourceType()}
     * @param id           internal database identifier
     */
    default void afterDelete(String resourceType, Long id) {
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
            public void beforeFindById(String resourceType, Long id) {
                delegates.forEach(hook -> hook.beforeFindById(resourceType, id));
            }

            @Override
            public void afterFindByIdResponse(String resourceType, DRS response, D domain) {
                delegates.forEach(hook -> hook.afterFindByIdResponse(resourceType, response, domain));
            }

            @Override
            public void beforeFindBy(String resourceType, GenericFilter filter) {
                delegates.forEach(hook -> hook.beforeFindBy(resourceType, filter));
            }

            @Override
            public void afterFindByResponse(String resourceType, DRS response, D domain) {
                delegates.forEach(hook -> hook.afterFindByResponse(resourceType, response, domain));
            }

            @Override
            public void beforeFindAll(String resourceType, GenericFilter filter, List<SortOrder> orders) {
                delegates.forEach(hook -> hook.beforeFindAll(resourceType, filter, orders));
            }

            @Override
            public void afterFindAllResponse(String resourceType, List<SRS> response, List<D> domains) {
                delegates.forEach(hook -> hook.afterFindAllResponse(resourceType, response, domains));
            }

            @Override
            public void beforeFindAllPaged(
                    String resourceType,
                    int page,
                    int size,
                    GenericFilter filter,
                    List<SortOrder> orders) {
                delegates.forEach(hook -> hook.beforeFindAllPaged(resourceType, page, size, filter, orders));
            }

            @Override
            public void afterFindAllPagedResponse(
                    String resourceType,
                    PageResult<SRS> response,
                    PageResult<D> domains) {
                delegates.forEach(hook -> hook.afterFindAllPagedResponse(resourceType, response, domains));
            }

            @Override
            public void beforeCreateRequest(String resourceType, CRQ request) {
                delegates.forEach(hook -> hook.beforeCreateRequest(resourceType, request));
            }

            @Override
            public void beforeCreate(String resourceType, D domain, CRQ request) {
                delegates.forEach(hook -> hook.beforeCreate(resourceType, domain, request));
            }

            @Override
            public void afterCreate(String resourceType, D saved, CRQ request) {
                delegates.forEach(hook -> hook.afterCreate(resourceType, saved, request));
            }

            @Override
            public void afterCreateResponse(String resourceType, DRS response, D saved, CRQ request) {
                delegates.forEach(hook -> hook.afterCreateResponse(resourceType, response, saved, request));
            }

            @Override
            public void beforeUpdateRequest(String resourceType, Long id, URQ request) {
                delegates.forEach(hook -> hook.beforeUpdateRequest(resourceType, id, request));
            }

            @Override
            public void beforeUpdate(String resourceType, D domain, URQ request) {
                delegates.forEach(hook -> hook.beforeUpdate(resourceType, domain, request));
            }

            @Override
            public void afterUpdate(String resourceType, D saved, URQ request) {
                delegates.forEach(hook -> hook.afterUpdate(resourceType, saved, request));
            }

            @Override
            public void afterUpdateResponse(String resourceType, DRS response, D saved, URQ request) {
                delegates.forEach(hook -> hook.afterUpdateResponse(resourceType, response, saved, request));
            }

            @Override
            public void beforeDeleteRequest(String resourceType, Long id) {
                delegates.forEach(hook -> hook.beforeDeleteRequest(resourceType, id));
            }

            @Override
            public void beforeDelete(String resourceType, Long id) {
                delegates.forEach(hook -> hook.beforeDelete(resourceType, id));
            }

            @Override
            public void afterDelete(String resourceType, Long id) {
                delegates.forEach(hook -> hook.afterDelete(resourceType, id));
            }
        };
    }
}
