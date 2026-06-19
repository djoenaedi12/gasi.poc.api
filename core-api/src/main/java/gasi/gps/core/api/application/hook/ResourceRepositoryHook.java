package gasi.gps.core.api.application.hook;

import java.util.List;
import java.util.Optional;

import gasi.gps.core.api.domain.model.BaseModel;
import gasi.gps.core.api.domain.model.GenericFilter;
import gasi.gps.core.api.domain.model.PageResult;
import gasi.gps.core.api.domain.model.SortOrder;

/**
 * Lifecycle hook contract for resource repository operations.
 *
 * @param <D> domain model type
 * @since 1.0.0
 */
public interface ResourceRepositoryHook<D extends BaseModel> {

    default D beforeSave(D model) {
        return model;
    }

    default void afterSave(D saved) {
    }

    default List<D> beforeSaveAll(List<D> models) {
        return models;
    }

    default void afterSaveAll(List<D> saved) {
    }

    default void beforeFindById(Long id) {
    }

    default void afterFindById(Optional<D> result, Long id) {
    }

    default GenericFilter beforeFindBy(GenericFilter filter) {
        return filter;
    }

    default void afterFindBy(Optional<D> result, GenericFilter filter) {
    }

    default GenericFilter beforeFindAll(GenericFilter filter, List<SortOrder> orders) {
        return filter;
    }

    default void afterFindAll(List<D> result, GenericFilter filter, List<SortOrder> orders) {
    }

    default GenericFilter beforeFindAllPaged(int page, int size, GenericFilter filter, List<SortOrder> orders) {
        return filter;
    }

    default void afterFindAllPaged(PageResult<D> result, int page, int size, GenericFilter filter,
            List<SortOrder> orders) {
    }

    default void beforeDelete(Long id) {
    }

    default void afterDelete(Long id) {
    }

    default List<Long> beforeDeleteAllByIds(List<Long> ids) {
        return ids;
    }

    default void afterDeleteAllByIds(List<Long> ids) {
    }

    default GenericFilter beforeDeleteAllBy(GenericFilter filter) {
        return filter;
    }

    default void afterDeleteAllBy(GenericFilter filter) {
    }

    static <D extends BaseModel> ResourceRepositoryHook<D> noop() {
        return new ResourceRepositoryHook<>() {
        };
    }

    static <D extends BaseModel> ResourceRepositoryHook<D> composite(
            List<? extends ResourceRepositoryHook<D>> hooks) {
        if (hooks == null || hooks.isEmpty()) {
            return noop();
        }
        return new ResourceRepositoryHook<>() {
            @Override
            public D beforeSave(D model) {
                D next = model;
                for (ResourceRepositoryHook<D> hook : hooks) {
                    next = hook.beforeSave(next);
                }
                return next;
            }

            @Override
            public void afterSave(D saved) {
                hooks.forEach(hook -> hook.afterSave(saved));
            }

            @Override
            public List<D> beforeSaveAll(List<D> models) {
                List<D> next = models;
                for (ResourceRepositoryHook<D> hook : hooks) {
                    next = hook.beforeSaveAll(next);
                }
                return next;
            }

            @Override
            public void afterSaveAll(List<D> saved) {
                hooks.forEach(hook -> hook.afterSaveAll(saved));
            }

            @Override
            public void beforeFindById(Long id) {
                hooks.forEach(hook -> hook.beforeFindById(id));
            }

            @Override
            public void afterFindById(Optional<D> result, Long id) {
                hooks.forEach(hook -> hook.afterFindById(result, id));
            }

            @Override
            public GenericFilter beforeFindBy(GenericFilter filter) {
                GenericFilter next = filter;
                for (ResourceRepositoryHook<D> hook : hooks) {
                    next = hook.beforeFindBy(next);
                }
                return next;
            }

            @Override
            public void afterFindBy(Optional<D> result, GenericFilter filter) {
                hooks.forEach(hook -> hook.afterFindBy(result, filter));
            }

            @Override
            public GenericFilter beforeFindAll(GenericFilter filter, List<SortOrder> orders) {
                GenericFilter next = filter;
                for (ResourceRepositoryHook<D> hook : hooks) {
                    next = hook.beforeFindAll(next, orders);
                }
                return next;
            }

            @Override
            public void afterFindAll(List<D> result, GenericFilter filter, List<SortOrder> orders) {
                hooks.forEach(hook -> hook.afterFindAll(result, filter, orders));
            }

            @Override
            public GenericFilter beforeFindAllPaged(int page, int size, GenericFilter filter, List<SortOrder> orders) {
                GenericFilter next = filter;
                for (ResourceRepositoryHook<D> hook : hooks) {
                    next = hook.beforeFindAllPaged(page, size, next, orders);
                }
                return next;
            }

            @Override
            public void afterFindAllPaged(PageResult<D> result, int page, int size, GenericFilter filter,
                    List<SortOrder> orders) {
                hooks.forEach(hook -> hook.afterFindAllPaged(result, page, size, filter, orders));
            }

            @Override
            public void beforeDelete(Long id) {
                hooks.forEach(hook -> hook.beforeDelete(id));
            }

            @Override
            public void afterDelete(Long id) {
                hooks.forEach(hook -> hook.afterDelete(id));
            }

            @Override
            public List<Long> beforeDeleteAllByIds(List<Long> ids) {
                List<Long> next = ids;
                for (ResourceRepositoryHook<D> hook : hooks) {
                    next = hook.beforeDeleteAllByIds(next);
                }
                return next;
            }

            @Override
            public void afterDeleteAllByIds(List<Long> ids) {
                hooks.forEach(hook -> hook.afterDeleteAllByIds(ids));
            }

            @Override
            public GenericFilter beforeDeleteAllBy(GenericFilter filter) {
                GenericFilter next = filter;
                for (ResourceRepositoryHook<D> hook : hooks) {
                    next = hook.beforeDeleteAllBy(next);
                }
                return next;
            }

            @Override
            public void afterDeleteAllBy(GenericFilter filter) {
                hooks.forEach(hook -> hook.afterDeleteAllBy(filter));
            }
        };
    }
}
