package gasi.gps.core.starter.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import gasi.gps.core.api.application.exception.EntityNotFoundException;
import gasi.gps.core.api.application.hook.ResourceMapperHook;
import gasi.gps.core.api.application.hook.ResourceServiceHook;
import gasi.gps.core.api.domain.model.BaseModel;
import gasi.gps.core.api.domain.model.GenericFilter;
import gasi.gps.core.api.domain.model.PageResult;
import gasi.gps.core.api.domain.model.SortOrder;
import gasi.gps.core.api.domain.port.inbound.BaseReadService;
import gasi.gps.core.api.domain.port.outbound.BaseRepositoryPort;
import gasi.gps.core.starter.application.hook.ResourceMapperHookRegistry;
import gasi.gps.core.starter.application.hook.ResourceServiceHookRegistry;
import gasi.gps.core.starter.application.mapper.BaseReadDtoMapper;
import gasi.gps.core.starter.infrastructure.i18n.MessageUtil;
import gasi.gps.core.starter.infrastructure.util.IdEncoder;

/**
 * Generic transactional implementation of {@link BaseReadService}.
 *
 * <p>
 * Subclasses supply concrete repository and read mapper implementations,
 * then override {@link #resourceType()} for resource-specific error
 * messages.
 * </p>
 *
 * @param <D>   domain model type
 * @param <SRS> summary response DTO type
 * @param <DRS> detail response DTO type
 * @since 1.0.0
 */
@Transactional(readOnly = true)
public abstract class BaseReadServiceImpl<D extends BaseModel, SRS, DRS>
        implements BaseReadService<SRS, DRS> {

    /** Repository port used by the service implementation. */
    protected final BaseRepositoryPort<D> repositoryPort;

    /** Mapper used to convert domain models into response DTOs. */
    protected final BaseReadDtoMapper<D, SRS, DRS> mapper;

    /** Localized message helper for user-facing errors. */
    protected final MessageUtil messageUtil;

    /** Public ID encoder used in responses and error messages. */
    protected final IdEncoder idEncoder;

    /** Registry for generated and custom resource service hooks. */
    private final ResourceServiceHookRegistry hookRegistry;

    /** Registry for generated and custom resource mapper hooks. */
    private final ResourceMapperHookRegistry mapperHookRegistry;

    /**
     * Creates a base read service implementation with ordered resource and mapper
     * hooks.
     *
     * @param repositoryPort     repository port for domain persistence
     * @param mapper             mapper from domain models to response DTOs
     * @param messageUtil        localized message helper
     * @param idEncoder          public ID encoder
     * @param hookRegistry       registry for generated and custom service hooks
     * @param mapperHookRegistry registry for generated and custom mapper hooks
     */
    protected BaseReadServiceImpl(BaseRepositoryPort<D> repositoryPort,
            BaseReadDtoMapper<D, SRS, DRS> mapper,
            MessageUtil messageUtil,
            IdEncoder idEncoder,
            ResourceServiceHookRegistry hookRegistry,
            ResourceMapperHookRegistry mapperHookRegistry) {
        this.repositoryPort = repositoryPort;
        this.mapper = mapper;
        this.messageUtil = messageUtil;
        this.idEncoder = idEncoder;
        this.hookRegistry = hookRegistry;
        this.mapperHookRegistry = mapperHookRegistry;
    }

    @Override
    public DRS findById(Long id) {
        ResourceServiceHook<D, Object, Object, SRS, DRS> hook = serviceHook();
        hook.beforeFindById(id);
        D domain = repositoryPort.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageUtil.get("error.entity.notFound", resourceType(), idEncoder.encode(id))));
        DRS response = toDetailResponse(domain);
        hook.afterFindByIdResponse(response, domain);
        return response;
    }

    @Override
    public DRS findBy(GenericFilter filter) {
        ResourceServiceHook<D, Object, Object, SRS, DRS> hook = serviceHook();
        hook.beforeFindBy(filter);
        D domain = repositoryPort.findBy(filter)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageUtil.get("error.entity.notFound", resourceType(), "filter")));
        DRS response = toDetailResponse(domain);
        hook.afterFindByResponse(response, domain);
        return response;
    }

    @Override
    public List<SRS> findAll(GenericFilter filter, List<SortOrder> orders) {
        ResourceServiceHook<D, Object, Object, SRS, DRS> hook = serviceHook();
        hook.beforeFindAll(filter, orders);
        List<D> result = repositoryPort.findAll(filter, orders);
        List<SRS> response = result.stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
        hook.afterFindAllResponse(response, result);
        return response;
    }

    @Override
    public PageResult<SRS> findAll(int page, int size, GenericFilter filter, List<SortOrder> orders) {
        ResourceServiceHook<D, Object, Object, SRS, DRS> hook = serviceHook();
        hook.beforeFindAllPaged(page, size, filter, orders);
        PageResult<D> result = repositoryPort.findAll(page, size, filter, orders);
        List<SRS> content = result.getContent().stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
        PageResult<SRS> response = PageResult.<SRS>builder()
                .content(content)
                .page(result.getPage())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
        hook.afterFindAllPagedResponse(response, result);
        return response;
    }

    /**
     * Returns a human-readable resource type for error messages.
     *
     * @return resource type name
     */
    protected String resourceType() {
        return "Entity";
    }

    /**
     * Enrich a detail response with additional data (child lists, computed fields).
     * Called on create, update, and findById.
     *
     * @param response the detail response to enrich
     * @return the enriched response
     */
    protected DRS enrichDetail(DRS response) {
        return response;
    }

    /**
     * Convert saved domain to detail response, then enrich.
     *
     * @param saved the persisted domain object
     * @return the enriched detail response
     */
    protected DRS toDetailResponse(D saved) {
        DRS response = mapper.toDetail(saved);
        mapperHook().afterToDetail(response, saved);
        return enrichDetail(response);
    }

    /**
     * Convert a domain object to summary response, then run mapper hooks.
     *
     * @param domain domain object
     * @return summary response
     */
    protected SRS toSummaryResponse(D domain) {
        SRS response = mapper.toSummary(domain);
        mapperHook().afterToSummary(response, domain);
        return response;
    }

    /**
     * Resolves ordered resource hooks for this service.
     *
     * @param <CRQ> create request DTO type
     * @param <URQ> update request DTO type
     * @return composite hook, or no-op when no registry/hook is available
     */
    protected <CRQ, URQ> ResourceServiceHook<D, CRQ, URQ, SRS, DRS> serviceHook() {
        if (hookRegistry == null) {
            return ResourceServiceHook.noop();
        }
        return hookRegistry.resolve(resourceType());
    }

    /**
     * Resolves ordered mapper hooks for this service.
     *
     * @param <CRQ> create request DTO type
     * @param <URQ> update request DTO type
     * @return composite hook, or no-op when no registry/hook is available
     */
    protected <CRQ, URQ> ResourceMapperHook<D, CRQ, URQ, SRS, DRS> mapperHook() {
        if (mapperHookRegistry == null) {
            return ResourceMapperHook.noop();
        }
        return mapperHookRegistry.resolve(resourceType());
    }
}
