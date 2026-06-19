package gasi.gps.core.starter.infrastructure.adapter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import gasi.gps.core.api.domain.model.BaseModel;
import gasi.gps.core.api.domain.model.GenericFilter;
import gasi.gps.core.api.domain.model.PageResult;
import gasi.gps.core.api.domain.model.SimpleFilter;
import gasi.gps.core.api.domain.model.SortOrder;
import gasi.gps.core.api.domain.port.outbound.BaseRepositoryPort;
import gasi.gps.core.api.application.hook.ResourceRepositoryHook;
import gasi.gps.core.starter.application.hook.ResourceRepositoryHookRegistry;
import gasi.gps.core.starter.infrastructure.entity.BaseEntity;
import gasi.gps.core.starter.infrastructure.filter.FilterableFieldResolver;
import gasi.gps.core.starter.infrastructure.mapper.BaseMapper;
import gasi.gps.core.starter.infrastructure.specification.GenericSpecification;

/**
 * Generic Spring Data JPA adapter for {@link BaseRepositoryPort}.
 *
 * <p>This adapter handles common CRUD, filtering, sorting, pagination, entity
 * mapping, and public filter-field validation. Record-rule support is exposed
 * through method parameters and can be added by extending the record-rule
 * hook.</p>
 *
 * @param <D>  domain model type
 * @param <E>  JPA entity type
 * @since 1.0.0
 */
public abstract class BaseRepositoryAdapter<D extends BaseModel, E extends BaseEntity>
        implements BaseRepositoryPort<D> {

    private final JpaRepository<E, Long> jpaRepository;
    private final JpaSpecificationExecutor<E> specExecutor;
    private final BaseMapper<D, E> mapper;
    private final Class<E> entityClass;
    private final ResourceRepositoryHookRegistry hookRegistry;

    /**
     * Creates an adapter with an explicit entity class and ordered repository hooks.
     *
     * @param repository   Spring Data repository implementing JPA and specification
     *                     contracts
     * @param mapper       mapper between domain model and JPA entity
     * @param entityClass  JPA entity class
     * @param hookRegistry registry for generated and custom repository hooks
     * @param <R>          repository type
     */
    protected <R extends JpaRepository<E, Long> & JpaSpecificationExecutor<E>> BaseRepositoryAdapter(R repository,
            BaseMapper<D, E> mapper,
            Class<E> entityClass,
            ResourceRepositoryHookRegistry hookRegistry) {
        this.jpaRepository = repository;
        this.specExecutor = repository;
        this.mapper = mapper;
        this.entityClass = entityClass != null ? entityClass : resolveEntityClass();
        this.hookRegistry = hookRegistry;
    }

    /**
     * Returns the resource type used for record-rule resolution.
     *
     * @return resource type code expected by the record-rule implementation
     */
    protected abstract String resourceType();

    // ── Save ──────────────────────────────────────────────

    @Override
    public D save(D model) {
        ResourceRepositoryHook<D> hook = repositoryHook();
        D modelToSave = hook.beforeSave(model);
        E entity = mapper.toEntity(modelToSave);
        E saved = jpaRepository.save(entity);
        D result = mapper.toDomain(saved);
        hook.afterSave(result);
        return result;
    }

    @Override
    public List<D> saveAll(List<D> models) {
        ResourceRepositoryHook<D> hook = repositoryHook();
        List<D> modelsToSave = hook.beforeSaveAll(models);
        List<E> entities = modelsToSave.stream()
                .map(mapper::toEntity)
                .toList();
        List<D> result = jpaRepository.saveAll(entities).stream()
                .map(mapper::toDomain)
                .toList();
        hook.afterSaveAll(result);
        return result;
    }

    // ── Find single ───────────────────────────────────────

    @Override
    public Optional<D> findById(Long id) {
        return findById(id, true);
    }

    @Override
    public Optional<D> findById(Long id, boolean useRecordRule) {
        ResourceRepositoryHook<D> hook = repositoryHook();
        hook.beforeFindById(id);
        GenericFilter idFilter = SimpleFilter.builder()
                .field("id")
                .operator(SimpleFilter.FilterOperator.EQUALS)
                .value(id)
                .build();
        Optional<D> result = findByInternal(idFilter, useRecordRule);
        hook.afterFindById(result, id);
        return result;
    }

    @Override
    public Optional<D> findBy(GenericFilter filter) {
        return findBy(filter, true);
    }

    @Override
    public Optional<D> findBy(GenericFilter filter, boolean useRecordRule) {
        ResourceRepositoryHook<D> hook = repositoryHook();
        GenericFilter hookFilter = hook.beforeFindBy(filter);
        Optional<D> result = findByInternal(hookFilter, useRecordRule);
        hook.afterFindBy(result, hookFilter);
        return result;
    }

    // ── Find all ──────────────────────────────────────────

    @Override
    public List<D> findAll(GenericFilter filter, List<SortOrder> orders) {
        return findAll(filter, orders, true);
    }

    @Override
    public List<D> findAll(GenericFilter filter, List<SortOrder> orders, boolean useRecordRule) {
        ResourceRepositoryHook<D> hook = repositoryHook();
        GenericFilter hookFilter = hook.beforeFindAll(filter, orders);
        GenericFilter effectiveFilter = useRecordRule ? applyRecordRules(hookFilter) : hookFilter;
        Specification<E> spec = toSpec(effectiveFilter);
        Sort sort = toSort(orders);
        List<D> result = specExecutor.findAll(spec, sort).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
        hook.afterFindAll(result, hookFilter, orders);
        return result;
    }

    // ── Find all with pagination ──────────────────────────

    @Override
    public PageResult<D> findAll(int page, int size, GenericFilter filter, List<SortOrder> orders) {
        return findAll(page, size, filter, orders, true);
    }

    @Override
    public PageResult<D> findAll(int page, int size, GenericFilter filter, List<SortOrder> orders,
            boolean useRecordRule) {
        ResourceRepositoryHook<D> hook = repositoryHook();
        GenericFilter hookFilter = hook.beforeFindAllPaged(page, size, filter, orders);
        GenericFilter effectiveFilter = useRecordRule ? applyRecordRules(hookFilter) : hookFilter;
        Specification<E> spec = toSpec(effectiveFilter);
        PageRequest pageable = PageRequest.of(page, size, toSort(orders));
        Page<E> result = specExecutor.findAll(spec, pageable);

        List<D> content = result.getContent().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());

        PageResult<D> pageResult = PageResult.<D>builder()
                .content(content)
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
        hook.afterFindAllPaged(pageResult, page, size, hookFilter, orders);
        return pageResult;
    }

    // ── Delete ────────────────────────────────────────────

    @Override
    public void delete(Long id) {
        ResourceRepositoryHook<D> hook = repositoryHook();
        hook.beforeDelete(id);
        jpaRepository.deleteById(id);
        hook.afterDelete(id);
    }

    @Override
    public void deleteAllByIds(List<Long> ids) {
        ResourceRepositoryHook<D> hook = repositoryHook();
        List<Long> idsToDelete = hook.beforeDeleteAllByIds(ids);
        if (idsToDelete == null || idsToDelete.isEmpty()) {
            return;
        }
        jpaRepository.deleteAllById(idsToDelete);
        hook.afterDeleteAllByIds(idsToDelete);
    }

    @Override
    public void deleteAllBy(GenericFilter filter) {
        deleteAllBy(filter, true);
    }

    @Override
    public void deleteAllBy(GenericFilter filter, boolean useRecordRule) {
        if (filter == null) {
            throw new IllegalArgumentException("GenericFilter must not be null for bulk delete");
        }

        ResourceRepositoryHook<D> hook = repositoryHook();
        GenericFilter hookFilter = hook.beforeDeleteAllBy(filter);
        GenericFilter effectiveFilter = useRecordRule ? applyRecordRules(hookFilter) : hookFilter;
        Specification<E> spec = toSpec(effectiveFilter);
        List<E> entities = specExecutor.findAll(spec);

        if (entities.isEmpty()) {
            return;
        }

        jpaRepository.deleteAllInBatch(entities);
        hook.afterDeleteAllBy(hookFilter);
    }

    private Optional<D> findByInternal(GenericFilter filter, boolean useRecordRule) {
        GenericFilter effectiveFilter = useRecordRule ? applyRecordRules(filter) : filter;
        Specification<E> spec = GenericSpecification.from(effectiveFilter);
        return specExecutor.findOne(spec).map(mapper::toDomain);
    }

    // ── Record Rules ─────────────────────────────────────

    private GenericFilter applyRecordRules(GenericFilter filter) {
        return filter;
        // if (!securityContext.isAuthenticated()) {
        // return userFilter;
        // }
        // GenericFilter ruleFilter = recordRuleService.resolveFilter(
        // securityContext.getRoleIds(),
        // resourceType(),
        // securityContext.getRequestUri());
        // return recordRuleService.combineWithUserFilter(userFilter, ruleFilter);
    }

    // ── Helpers ───────────────────────────────────────────

    private Specification<E> toSpec(GenericFilter filter) {
        return GenericSpecification.from(filter);
    }

    private Sort toSort(List<SortOrder> orders) {
        if (orders == null || orders.isEmpty()) {
            return defaultSort();
        }

        List<Sort.Order> sortOrders = orders.stream()
                .filter(s -> s.getField() != null && !s.getField().isBlank())
                .map(order -> new Sort.Order(
                        Sort.Direction.fromString(order.getDirection().toString()),
                        FilterableFieldResolver.resolve(entityClass, order.getField())))
                .collect(Collectors.toList());

        if (sortOrders.isEmpty()) {
            return defaultSort();
        }
        if (sortOrders.stream().noneMatch(order -> "id".equals(order.getProperty()))) {
            sortOrders.add(defaultIdOrder());
        }
        return Sort.by(sortOrders);
    }

    private Sort defaultSort() {
        return Sort.by(defaultIdOrder());
    }

    private Sort.Order defaultIdOrder() {
        return Sort.Order.asc("id");
    }

    private ResourceRepositoryHook<D> repositoryHook() {
        if (hookRegistry == null) {
            return ResourceRepositoryHook.noop();
        }
        return hookRegistry.resolve(resourceType());
    }

    @SuppressWarnings("unchecked")
    private Class<E> resolveEntityClass() {
        Type current = getClass().getGenericSuperclass();
        while (current != null) {
            if (current instanceof ParameterizedType parameterizedType
                    && parameterizedType.getRawType() instanceof Class<?> rawType
                    && BaseRepositoryAdapter.class.isAssignableFrom(rawType)) {
                Type entityType = parameterizedType.getActualTypeArguments()[1];
                if (entityType instanceof Class<?> entityClassType) {
                    return (Class<E>) entityClassType;
                }
            }

            if (current instanceof Class<?> currentClass) {
                current = currentClass.getGenericSuperclass();
            } else {
                break;
            }
        }
        throw new IllegalStateException("Unable to resolve entity class for " + getClass().getName()
                + ". Use the BaseRepositoryAdapter(repository, mapper, entityClass) constructor.");
    }
}
