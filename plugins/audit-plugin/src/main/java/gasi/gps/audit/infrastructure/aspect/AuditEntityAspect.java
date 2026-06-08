package gasi.gps.audit.infrastructure.aspect;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import gasi.gps.audit.AuditContext;
import gasi.gps.audit.domain.model.AuditLog;
import gasi.gps.audit.domain.port.outbound.AuditLogRepositoryPort;
import gasi.gps.core.api.application.dto.BaseSummaryResponse;
import gasi.gps.core.api.audit.AuditLogExtension;
import gasi.gps.core.api.audit.AuditableEntity;
import gasi.gps.core.api.domain.port.inbound.BaseReadService;
import gasi.gps.core.api.security.SecurityContextProvider;
import gasi.gps.core.starter.infrastructure.entity.BaseEntity;
import gasi.gps.core.starter.infrastructure.util.IdEncoder;

/**
 * AOP Aspect that intercepts BaseService CUD methods and generates audit logs
 * for services annotated with @AuditableEntity.
 *
 * <p>
 * Handles nested call detection via AuditContext ThreadLocal:
 * - Default (alwaysLog = false): skip logging if already inside another audited
 * call
 * - alwaysLog = true: always log regardless of nesting
 * </p>
 *
 * @since 1.0.0
 */
@Aspect
@Component
@Order(1)
public class AuditEntityAspect {

    private static final Logger LOG = LoggerFactory.getLogger(AuditEntityAspect.class);
    private static final String[] NO_CHANGED_FIELDS = new String[0];
    private static final Set<String> IGNORED_CHANGE_FIELDS = Set.of(
            "id",
            "createdAt",
            "updatedAt",
            "createdBy",
            "updatedBy",
            "sourceId",
            "lifecycleStatus",
            "version");

    private final AuditLogRepositoryPort repository;
    private final SecurityContextProvider securityContextUtil;
    private final PluginManager pluginManager;
    private final IdEncoder idEncoder;

    /**
     * Creates the entity-level audit aspect.
     *
     * @param repository          audit log repository
     * @param securityContextUtil current security context provider
     * @param pluginManager       PF4J plugin manager for audit extensions
     * @param idEncoder           encoded ID codec
     */
    public AuditEntityAspect(AuditLogRepositoryPort repository,
            SecurityContextProvider securityContextUtil,
            PluginManager pluginManager,
            IdEncoder idEncoder) {
        this.repository = repository;
        this.securityContextUtil = securityContextUtil;
        this.pluginManager = pluginManager;
        this.idEncoder = idEncoder;
    }

    // ─── CREATE ──────────────────────────────────────────────────────

    /**
     * Writes a create audit log after a service create call returns.
     *
     * @param joinPoint join point for the create call
     * @param target    service target object
     * @param result    created object returned by the service
     */
    @AfterReturning(pointcut = "execution(* gasi.gps..BaseService+.create(..)) && target(target)", returning = "result")
    public void auditCreate(JoinPoint joinPoint, Object target, Object result) {
        doLog(target, "CREATE", result);
    }

    // ─── UPDATE ──────────────────────────────────────────────────────

    /**
     * Writes an update audit log around a service update call.
     *
     * @param joinPoint proceeding join point for the update call
     * @param target    service target object
     * @return result returned by the update call
     * @throws Throwable if the audited update call fails
     */
    @Around("execution(* gasi.gps..BaseService+.update(..)) && target(target)")
    public Object auditUpdate(ProceedingJoinPoint joinPoint, Object target) throws Throwable {
        Object idArg = joinPoint.getArgs().length > 0 ? joinPoint.getArgs()[0] : null;
        Long id = idArg instanceof Long longId ? longId : null;
        Object before = findDetailBeforeUpdate(target, id);
        Object result = joinPoint.proceed();
        String[] changedFields = resolveChangedFields(before, result);
        doLogWithId(target, "UPDATE", idArg != null ? idArg.toString() : null, changedFields);
        return result;
    }

    // ─── DELETE ──────────────────────────────────────────────────────

    /**
     * Writes a delete audit log after a service delete call returns.
     *
     * @param joinPoint join point for the delete call
     * @param target    service target object
     */
    @AfterReturning(pointcut = "execution(* gasi.gps..BaseService+.delete(..)) && target(target)")
    public void auditDelete(JoinPoint joinPoint, Object target) {
        Object idArg = joinPoint.getArgs().length > 0 ? joinPoint.getArgs()[0] : null;
        doLogWithId(target, "DELETE", idArg != null ? idArg.toString() : null);
    }

    // ─── FAILURE ─────────────────────────────────────────────────────

    /**
     * Writes a failure audit log when a CUD service method throws.
     *
     * @param joinPoint join point for the failing call
     * @param target    service target object
     * @param ex        thrown exception
     */
    @AfterThrowing(pointcut = "execution(* gasi.gps..BaseService+.create(..)) && target(target)"
            + " || execution(* gasi.gps..BaseService+.update(..)) && target(target)"
            + " || execution(* gasi.gps..BaseService+.delete(..)) && target(target)", throwing = "ex")
    public void auditFailure(JoinPoint joinPoint, Object target, Exception ex) {
        AuditableEntity annotation = target.getClass().getAnnotation(AuditableEntity.class);
        if (annotation == null) {
            return;
        }

        String action = resolveActionFromMethod(joinPoint.getSignature().getName());

        try {
            repository.save(AuditLog.builder()
                    .traceId(MDC.get("traceId"))
                    .actorId(securityContextUtil.getCurrentUsername())
                    .actorIp(securityContextUtil.getCurrentIp())
                    .action(action)
                    .module(annotation.module())
                    .resourceType(annotation.resourceType())
                    .description("Failed: " + ex.getMessage())
                    .status("FAILED")
                    .createdAt(Instant.now())
                    .build());
        } catch (Exception logEx) {
            LOG.error("Failed to write audit log for failure", logEx);
        }
    }

    // ─── Core Logic ──────────────────────────────────────────────────

    private String resolveEntityId(Object target) {
        if (target instanceof BaseEntity entity) {
            return entity.getId() != null ? entity.getId().toString() : null;
        } else if (target instanceof BaseSummaryResponse srs) {
            Long idLong = idEncoder.decode(srs.getId());
            return idLong != null ? idLong.toString() : null;
        }
        return null;
    }

    private void doLog(Object target, String action, Object result) {
        doLogWithId(target, action, resolveEntityId(result), NO_CHANGED_FIELDS);
    }

    private void doLogWithId(Object target, String action, String entityId) {
        doLogWithId(target, action, entityId, NO_CHANGED_FIELDS);
    }

    private void doLogWithId(Object target, String action, String entityId, String[] changedFields) {
        AuditableEntity annotation = target.getClass().getAnnotation(AuditableEntity.class);
        if (annotation == null) {
            return;
        }

        if (!shouldAudit(annotation, action) || isNestedAuditSuppressed(annotation)) {
            return;
        }

        boolean isRoot = !AuditContext.isActive();
        try {
            if (isRoot) {
                AuditContext.start();
            }

            String description = resolveDescription(annotation, action, entityId);
            repository.save(successAuditLog(annotation, action, entityId, description, changedFields));

        } catch (Exception e) {
            LOG.error("Failed to write audit log", e);
        } finally {
            if (isRoot) {
                AuditContext.clear();
            }
        }
    }

    private boolean shouldAudit(AuditableEntity annotation, String action) {
        return List.of(annotation.auditActions()).contains(action);
    }

    private boolean isNestedAuditSuppressed(AuditableEntity annotation) {
        return AuditContext.isActive() && !annotation.alwaysLog();
    }

    private AuditLog successAuditLog(
            AuditableEntity annotation,
            String action,
            String entityId,
            String description,
            String[] changedFields) {
        return AuditLog.builder()
                .traceId(MDC.get("traceId"))
                .actorId(securityContextUtil.getCurrentUsername())
                .actorIp(securityContextUtil.getCurrentIp())
                .action(action)
                .module(annotation.module())
                .resourceType(annotation.resourceType())
                .resourceId(entityId)
                .description(description)
                .changedFields(changedFields != null ? changedFields : NO_CHANGED_FIELDS)
                .status("SUCCESS")
                .createdAt(Instant.now())
                .build();
    }

    private String resolveDescription(AuditableEntity annotation, String action, String entityId) {
        // Try plugin enrichers first
        List<AuditLogExtension> enrichers = pluginManager.getExtensions(AuditLogExtension.class);
        for (AuditLogExtension enricher : enrichers) {
            String desc = enricher.resolveDescription(action, annotation.resourceType(), entityId);
            if (desc != null) {
                return desc;
            }
        }

        // Default description
        return action + " " + annotation.resourceType()
                + (entityId != null ? "#" + entityId : "");
    }

    private Object findDetailBeforeUpdate(Object target, Long id) {
        if (id == null || !(target instanceof BaseReadService<?, ?> service)) {
            return null;
        }
        try {
            return service.findById(id);
        } catch (Exception e) {
            LOG.debug("Could not resolve audit snapshot before update", e);
            return null;
        }
    }

    private String[] resolveChangedFields(Object before, Object after) {
        if (before == null || after == null || !before.getClass().equals(after.getClass())) {
            return NO_CHANGED_FIELDS;
        }

        try {
            List<String> fields = new ArrayList<>();
            for (PropertyDescriptor descriptor : Introspector.getBeanInfo(after.getClass(), Object.class)
                    .getPropertyDescriptors()) {
                if (descriptor.getReadMethod() == null || IGNORED_CHANGE_FIELDS.contains(descriptor.getName())) {
                    continue;
                }
                Object beforeValue = descriptor.getReadMethod().invoke(before);
                Object afterValue = descriptor.getReadMethod().invoke(after);
                if (!Objects.equals(beforeValue, afterValue)) {
                    fields.add(descriptor.getName());
                }
            }
            fields.sort(Comparator.naturalOrder());
            return fields.isEmpty() ? NO_CHANGED_FIELDS : fields.toArray(String[]::new);
        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
            LOG.debug("Could not resolve changed fields for audit log", e);
            return NO_CHANGED_FIELDS;
        }
    }

    private String resolveActionFromMethod(String methodName) {
        return switch (methodName) {
            case "create" -> "CREATE";
            case "update" -> "UPDATE";
            case "delete" -> "DELETE";
            default -> methodName.toUpperCase();
        };
    }
}
