package gasi.gps.core.starter.application.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import gasi.gps.core.api.application.dto.VersionedRequest;
import gasi.gps.core.api.application.exception.BusinessException;
import gasi.gps.core.api.application.exception.EntityNotFoundException;
import gasi.gps.core.api.application.hook.ResourceMapperHook;
import gasi.gps.core.api.application.hook.ResourceServiceHook;
import gasi.gps.core.api.approval.ApprovalAction;
import gasi.gps.core.api.approval.ApprovalTarget;
import gasi.gps.core.api.domain.model.AndFilter;
import gasi.gps.core.api.domain.model.BaseModel;
import gasi.gps.core.api.domain.model.GenericFilter;
import gasi.gps.core.api.domain.model.LifecycleStatus;
import gasi.gps.core.api.domain.model.SimpleFilter;
import gasi.gps.core.api.domain.port.inbound.BaseService;
import gasi.gps.core.api.domain.port.inbound.MutationOptions;
import gasi.gps.core.api.domain.port.outbound.BaseRepositoryPort;
import gasi.gps.core.starter.application.approval.ApprovalExtensionRegistry;
import gasi.gps.core.starter.application.approval.ApprovalTargetHookRegistry;
import gasi.gps.core.starter.application.hook.ResourceMapperHookRegistry;
import gasi.gps.core.starter.application.hook.ResourceServiceHookRegistry;
import gasi.gps.core.starter.application.mapper.BaseDtoMapper;
import gasi.gps.core.starter.infrastructure.i18n.MessageUtil;
import gasi.gps.core.starter.infrastructure.util.IdEncoder;

/**
 * Generic transactional implementation of {@link BaseService}.
 *
 * <p>
 * This implementation builds on {@link BaseReadServiceImpl} for query
 * operations, then adds create, update, and delete behavior for full CRUD
 * resources. Subclasses supply concrete repository and mapper implementations.
 * Generated and custom lifecycle behavior can be added through ordered
 * {@link ResourceServiceHook} beans.
 * </p>
 *
 * @param <D>   domain model type
 * @param <CRQ> create request DTO
 * @param <URQ> update request DTO
 * @param <SRS> summary response DTO (for lists)
 * @param <DRS> detail response DTO (for single entity)
 * @since 1.0.0
 */
@Transactional
public abstract class BaseServiceImpl<D extends BaseModel, CRQ, URQ, SRS, DRS>
        extends BaseReadServiceImpl<D, SRS, DRS>
        implements BaseService<D, CRQ, URQ, SRS, DRS>, ApprovalTarget {

    /** Mapper used for write-side DTO conversion and inherited read mappings. */
    protected final BaseDtoMapper<D, CRQ, URQ, SRS, DRS> mapper;

    /** Optional approval extension registry contributed by the starter context. */
    private final ApprovalExtensionRegistry approvalExtensionRegistry;

    /** Registry for approval target hooks. */
    private final ApprovalTargetHookRegistry approvalTargetHookRegistry;

    /**
     * Creates a base service implementation with ordered resource and mapper hooks.
     *
     * @param repositoryPort            repository port for domain persistence
     * @param mapper                    mapper between request/response DTOs and
     *                                  domain
     *                                  models
     * @param messageUtil               localized message helper
     * @param idEncoder                 public ID encoder
     * @param hookRegistry              registry for generated and custom service
     *                                  hooks
     * @param mapperHookRegistry        registry for generated and custom mapper
     *                                  hooks
     * @param approvalExtensionRegistry registry for approval plugin extensions
     */
    protected BaseServiceImpl(BaseRepositoryPort<D> repositoryPort,
            BaseDtoMapper<D, CRQ, URQ, SRS, DRS> mapper,
            MessageUtil messageUtil,
            IdEncoder idEncoder,
            ResourceServiceHookRegistry hookRegistry,
            ResourceMapperHookRegistry mapperHookRegistry,
            ApprovalExtensionRegistry approvalExtensionRegistry,
            ApprovalTargetHookRegistry approvalTargetHookRegistry) {
        super(repositoryPort, mapper, messageUtil, idEncoder, hookRegistry, mapperHookRegistry);
        this.mapper = mapper;
        this.approvalExtensionRegistry = approvalExtensionRegistry;
        this.approvalTargetHookRegistry = approvalTargetHookRegistry;
    }

    @Override
    public DRS create(CRQ request, MutationOptions options) {
        ResourceServiceHook<D, CRQ, URQ, SRS, DRS> hook = serviceHook();
        ResourceMapperHook<D, CRQ, URQ, SRS, DRS> mapperHook = mapperHook();
        hook.beforeCreateRequest(resourceType(), request);
        D domain = mapper.toCreateDomain(request);
        mapperHook.afterToCreateDomain(domain, request);
        hook.beforeCreate(resourceType(), domain, request);
        boolean approvalRequired = approvalRequired(ApprovalAction.CREATE, options);
        if (approvalRequired) {
            domain.setLifecycleStatus(LifecycleStatus.PENDING_CREATE);
        }
        D saved = repositoryPort.save(domain);
        if (approvalRequired) {
            approvalExtensionRegistry.submitCreate(resourceType(), saved.getId(), saved);
        }
        hook.afterCreate(resourceType(), saved, request);
        DRS response = toDetailResponse(saved);
        hook.afterCreateResponse(resourceType(), response, saved, request);
        return response;
    }

    @Override
    public DRS update(Long id, URQ request, MutationOptions options) {
        ResourceServiceHook<D, CRQ, URQ, SRS, DRS> hook = serviceHook();
        ResourceMapperHook<D, CRQ, URQ, SRS, DRS> mapperHook = mapperHook();
        hook.beforeUpdateRequest(resourceType(), id, request);
        D existing = findRequired(id);
        validateNoPendingApproval(existing);
        if (existing.getLifecycleStatus() == LifecycleStatus.DRAFT
                || !approvalRequired(ApprovalAction.UPDATE, options)) {
            return updateExisting(existing, request, hook, mapperHook);
        }

        D pending = createPendingUpdate(existing, request, mapperHook);
        hook.beforeUpdate(resourceType(), pending, request);
        D saved = repositoryPort.save(pending);
        approvalExtensionRegistry.submitUpdate(resourceType(), id, existing, saved);
        hook.afterUpdate(resourceType(), saved, request);
        DRS response = toDetailResponse(saved);
        hook.afterUpdateResponse(resourceType(), response, saved, request);
        return response;
    }

    @Override
    public void delete(Long id, MutationOptions options) {
        ResourceServiceHook<D, CRQ, URQ, SRS, DRS> hook = serviceHook();
        hook.beforeDeleteRequest(resourceType(), id);
        D existing = findRequired(id);
        validateNoPendingApproval(existing);
        hook.beforeDelete(resourceType(), id);
        if (approvalRequired(ApprovalAction.DELETE, options)) {
            existing.setLifecycleStatus(LifecycleStatus.PENDING_DELETE);
            D saved = repositoryPort.save(existing);
            approvalExtensionRegistry.submitDelete(resourceType(), id, saved);
            return;
        }
        repositoryPort.delete(id);
        hook.afterDelete(resourceType(), id);
    }

    private DRS updateExisting(D existing, URQ request,
            ResourceServiceHook<D, CRQ, URQ, SRS, DRS> hook,
            ResourceMapperHook<D, CRQ, URQ, SRS, DRS> mapperHook) {
        mapper.updateDomain(request, existing);
        mapperHook.afterUpdateDomain(existing, request);
        if (request instanceof VersionedRequest vr) {
            existing.setVersion(vr.getVersion());
        }
        hook.beforeUpdate(resourceType(), existing, request);
        D saved = repositoryPort.save(existing);
        hook.afterUpdate(resourceType(), saved, request);
        DRS response = toDetailResponse(saved);
        hook.afterUpdateResponse(resourceType(), response, saved, request);
        return response;
    }

    private D findRequired(Long id) {
        return repositoryPort.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageUtil.get("error.entity.notFound", resourceType(), idEncoder.encode(id))));
    }

    private D createPendingUpdate(
            D existing,
            URQ request,
            ResourceMapperHook<D, CRQ, URQ, SRS, DRS> mapperHook) {

        D pending = mapper.cloneDomain(existing);

        pending.setSourceId(existing.getId());
        pending.setLifecycleStatus(LifecycleStatus.PENDING_UPDATE);

        mapper.updateDomain(request, pending);
        mapperHook.afterUpdateDomain(pending, request);

        return pending;
    }

    private void validateNoPendingApproval(D existing) {
        LifecycleStatus status = existing.getLifecycleStatus();
        if (status == LifecycleStatus.PENDING_CREATE
                || status == LifecycleStatus.PENDING_UPDATE
                || status == LifecycleStatus.PENDING_DELETE) {
            throw new BusinessException("Data is waiting for approval");
        }
        if (hasPendingUpdateShadow(existing)) {
            throw new BusinessException("Data has pending update approval");
        }
    }

    private boolean hasPendingUpdateShadow(D existing) {
        if (existing.getId() == null) {
            return false;
        }
        GenericFilter filter = AndFilter.builder()
                .filters(List.of(
                        SimpleFilter.builder()
                                .field("sourceId")
                                .operator(SimpleFilter.FilterOperator.EQUALS)
                                .value(existing.getId())
                                .build(),
                        SimpleFilter.builder()
                                .field("lifecycleStatus")
                                .operator(SimpleFilter.FilterOperator.EQUALS)
                                .value(LifecycleStatus.PENDING_UPDATE)
                                .build()))
                .build();
        return repositoryPort.findBy(filter, false).isPresent();
    }

    private boolean approvalRequired(ApprovalAction action, MutationOptions options) {
        if (options != null && options.bypassApproval()) {
            return false;
        }
        return approvalExtensionRegistry.isRequired(resourceType(), action);
    }

    /**
     * Applies a final approved create request.
     *
     * <p>
     * The pending create record is activated after the approval workflow reaches
     * the final approved state.
     * </p>
     *
     * @param id internal database identifier of the pending create record
     */
    @Override
    @Transactional
    public void approveCreate(Long id) {
        D pending = findRequired(id);

        pending.setLifecycleStatus(LifecycleStatus.ACTIVE);

        D saved = repositoryPort.save(pending);

        approvalTargetHookRegistry.afterCreateApproved(resourceType(), saved);
    }

    /**
     * Applies a final approved update request.
     *
     * <p>
     * The pending update record is copied into the active source record, then the
     * pending update record is removed.
     * </p>
     *
     * @param id internal database identifier of the pending update record
     */
    @Override
    @Transactional
    public void approveUpdate(Long id) {
        D pending = findRequired(id);

        Long sourceId = pending.getSourceId();
        if (sourceId == null) {
            throw new BusinessException("Pending update source id is required");
        }

        D active = findRequired(sourceId);

        mapper.copyDomain(pending, active);
        active.setLifecycleStatus(LifecycleStatus.ACTIVE);

        D saved = repositoryPort.save(active);
        repositoryPort.delete(pending.getId());

        approvalTargetHookRegistry.afterUpdateApproved(resourceType(), saved, pending);
    }

    /**
     * Applies a final approved delete request.
     *
     * <p>
     * The active record is permanently deleted after the approval workflow reaches
     * the final approved state.
     * </p>
     *
     * @param id internal database identifier of the active record
     */
    @Override
    @Transactional
    public void approveDelete(Long id) {
        D active = findRequired(id);

        repositoryPort.delete(active.getId());

        approvalTargetHookRegistry.afterDeleteApproved(resourceType(), active);
    }

    /**
     * Applies a final rejected create request.
     *
     * <p>
     * The pending create record is removed because the approval request was
     * rejected.
     * </p>
     *
     * @param id internal database identifier of the pending create record
     */
    @Override
    @Transactional
    public void rejectCreate(Long id) {
        D pending = findRequired(id);

        repositoryPort.delete(pending.getId());

        approvalTargetHookRegistry.afterCreateRejected(resourceType(), pending);
    }

    /**
     * Applies a final rejected update request.
     *
     * <p>
     * The pending update record is removed because the approval request was
     * rejected. The active source record is not changed by this operation.
     * </p>
     *
     * @param id internal database identifier of the pending update record
     */
    @Override
    @Transactional
    public void rejectUpdate(Long id) {
        D pending = findRequired(id);

        repositoryPort.delete(pending.getId());

        approvalTargetHookRegistry.afterUpdateRejected(resourceType(), pending);
    }

    /**
     * Applies a final rejected delete request.
     *
     * <p>
     * The active record is restored from pending delete status back to active
     * status.
     * </p>
     *
     * @param id internal database identifier of the active record
     */
    @Override
    @Transactional
    public void rejectDelete(Long id) {
        D active = findRequired(id);

        active.setLifecycleStatus(LifecycleStatus.ACTIVE);

        D saved = repositoryPort.save(active);

        approvalTargetHookRegistry.afterDeleteRejected(resourceType(), saved);
    }

    @Override
    public String resourceType() {
        return super.resourceType();
    }

}
