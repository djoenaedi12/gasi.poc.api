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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
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
 * Intercepts standard CUD service operations for classes marked with
 * {@link AuditableEntity}.
 *
 * <p>The audit context is active for the complete service invocation. Nested
 * audited calls are therefore suppressed unless their annotation explicitly
 * enables {@code alwaysLog}.</p>
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
    private final SecurityContextProvider securityContextProvider;
    private final PluginManager pluginManager;
    private final IdEncoder idEncoder;

    /**
     * Creates the entity audit aspect.
     *
     * @param repository              audit log repository
     * @param securityContextProvider current security context provider
     * @param pluginManager           PF4J plugin manager for description extensions
     * @param idEncoder               encoded ID codec
     */
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "PluginManager is a shared Spring/PF4J infrastructure singleton")
    public AuditEntityAspect(AuditLogRepositoryPort repository,
            SecurityContextProvider securityContextProvider,
            PluginManager pluginManager,
            IdEncoder idEncoder) {
        this.repository = repository;
        this.securityContextProvider = securityContextProvider;
        this.pluginManager = pluginManager;
        this.idEncoder = idEncoder;
    }

    /**
     * Audits create, update, and delete operations exposed by a base service.
     *
     * @param joinPoint intercepted service invocation
     * @param target    concrete service instance
     * @return original service result
     * @throws Throwable when the service invocation fails
     */
    @Around("execution(* gasi.gps..BaseService+.create(..)) && target(target)"
            + " || execution(* gasi.gps..BaseService+.update(..)) && target(target)"
            + " || execution(* gasi.gps..BaseService+.delete(..)) && target(target)")
    public Object auditOperation(ProceedingJoinPoint joinPoint, Object target) throws Throwable {
        AuditableEntity annotation = findAnnotation(target);
        if (annotation == null) {
            return joinPoint.proceed();
        }

        String action = resolveAction(joinPoint.getSignature().getName());
        if (!shouldAudit(annotation, action)) {
            return joinPoint.proceed();
        }

        boolean nested = AuditContext.isActive();
        if (nested && !annotation.alwaysLog()) {
            return joinPoint.proceed();
        }

        return proceedAudited(joinPoint, target, annotation, action, nested);
    }

    private Object proceedAudited(ProceedingJoinPoint joinPoint,
            Object target,
            AuditableEntity annotation,
            String action,
            boolean nested) throws Throwable {
        boolean root = !nested;
        if (root) {
            AuditContext.start();
        }

        Object idArg = firstArgument(joinPoint);
        Object before = "UPDATE".equals(action)
                ? findDetailBeforeUpdate(target, asLong(idArg))
                : null;

        try {
            Object result = joinPoint.proceed();
            String resourceId = resolveResourceId(action, idArg, result);
            String[] changedFields = "UPDATE".equals(action)
                    ? resolveChangedFields(before, result)
                    : NO_CHANGED_FIELDS;
            writeSuccess(annotation, action, resourceId, changedFields);
            return result;
        } catch (Throwable throwable) {
            writeFailure(annotation, action, idArg, throwable);
            throw throwable;
        } finally {
            if (root) {
                AuditContext.clear();
            }
        }
    }

    private AuditableEntity findAnnotation(Object target) {
        return AnnotatedElementUtils.findMergedAnnotation(
                AopUtils.getTargetClass(target), AuditableEntity.class);
    }

    private void writeSuccess(AuditableEntity annotation,
            String action,
            String resourceId,
            String[] changedFields) {
        try {
            String description = resolveDescription(annotation, action, resourceId);
            repository.save(baseLog(annotation, action)
                    .resourceId(resourceId)
                    .description(description)
                    .changedFields(changedFields)
                    .status("SUCCESS")
                    .build());
        } catch (Exception exception) {
            LOG.error("Failed to write successful audit log", exception);
        }
    }

    private void writeFailure(AuditableEntity annotation,
            String action,
            Object idArg,
            Throwable throwable) {
        try {
            repository.save(baseLog(annotation, action)
                    .resourceId(idArg != null ? idArg.toString() : null)
                    .description("Failed: " + failureMessage(throwable))
                    .changedFields(NO_CHANGED_FIELDS)
                    .status("FAILED")
                    .build());
        } catch (Exception exception) {
            LOG.error("Failed to write failed audit log", exception);
        }
    }

    private AuditLog.AuditLogBuilder<?, ?> baseLog(AuditableEntity annotation, String action) {
        return AuditLog.builder()
                .traceId(MDC.get("traceId"))
                .actorId(securityContextProvider.getCurrentUsername())
                .actorIp(securityContextProvider.getCurrentIp())
                .action(action)
                .module(annotation.module())
                .resourceType(annotation.resourceType())
                .createdAt(Instant.now());
    }

    private String resolveResourceId(String action, Object idArg, Object result) {
        if ("CREATE".equals(action)) {
            return resolveEntityId(result);
        }
        return idArg != null ? idArg.toString() : null;
    }

    private String resolveEntityId(Object target) {
        if (target instanceof BaseEntity entity) {
            return entity.getId() != null ? entity.getId().toString() : null;
        }
        if (target instanceof BaseSummaryResponse response && response.getId() != null) {
            Long decodedId = idEncoder.decode(response.getId());
            return decodedId != null ? decodedId.toString() : null;
        }
        return null;
    }

    private String resolveDescription(AuditableEntity annotation, String action, String resourceId) {
        List<AuditLogExtension> extensions = pluginManager.getExtensions(AuditLogExtension.class);
        for (AuditLogExtension extension : extensions) {
            if (!annotation.module().equalsIgnoreCase(extension.supportedModule())) {
                continue;
            }
            String description = extension.resolveDescription(
                    action, annotation.resourceType(), resourceId);
            if (description != null) {
                return description;
            }
        }

        return action + " " + annotation.resourceType()
                + (resourceId != null ? "#" + resourceId : "");
    }

    private Object findDetailBeforeUpdate(Object target, Long id) {
        if (id == null || !(target instanceof BaseReadService<?, ?> service)) {
            return null;
        }
        try {
            return service.findById(id);
        } catch (Exception exception) {
            LOG.debug("Could not resolve audit snapshot before update", exception);
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
        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException exception) {
            LOG.debug("Could not resolve changed fields for audit log", exception);
            return NO_CHANGED_FIELDS;
        }
    }

    private boolean shouldAudit(AuditableEntity annotation, String action) {
        return List.of(annotation.auditActions()).contains(action);
    }

    private Object firstArgument(ProceedingJoinPoint joinPoint) {
        return joinPoint.getArgs().length > 0 ? joinPoint.getArgs()[0] : null;
    }

    private Long asLong(Object value) {
        return value instanceof Long longValue ? longValue : null;
    }

    private String resolveAction(String methodName) {
        return switch (methodName) {
            case "create" -> "CREATE";
            case "update" -> "UPDATE";
            case "delete" -> "DELETE";
            default -> methodName.toUpperCase();
        };
    }

    private String failureMessage(Throwable throwable) {
        String message = throwable.getMessage();
        return message != null && !message.isBlank()
                ? message
                : throwable.getClass().getSimpleName();
    }
}
