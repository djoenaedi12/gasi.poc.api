package gasi.gps.audit.infrastructure.aspect;

import java.lang.reflect.Method;
import java.time.Instant;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import gasi.gps.audit.AuditContext;
import gasi.gps.audit.domain.model.AuditLog;
import gasi.gps.audit.domain.port.outbound.AuditLogRepositoryPort;
import gasi.gps.core.api.application.dto.BaseSummaryResponse;
import gasi.gps.core.api.audit.Auditable;
import gasi.gps.core.api.security.SecurityContextProvider;
import gasi.gps.core.starter.infrastructure.entity.BaseEntity;
import gasi.gps.core.starter.infrastructure.util.IdEncoder;

/**
 * Audits business operations marked with {@link Auditable}.
 *
 * @since 1.0.0
 */
@Aspect
@Component
@Order(2)
public class AuditMethodAspect {

    private static final Logger LOG = LoggerFactory.getLogger(AuditMethodAspect.class);

    private final AuditLogRepositoryPort repository;
    private final SecurityContextProvider securityContextProvider;
    private final IdEncoder idEncoder;
    private final ExpressionParser spelParser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer parameterNameDiscoverer =
            new DefaultParameterNameDiscoverer();

    /**
     * Creates the method-level audit aspect.
     *
     * @param repository              audit log repository
     * @param securityContextProvider current security context provider
     * @param idEncoder               encoded ID codec
     */
    public AuditMethodAspect(AuditLogRepositoryPort repository,
            SecurityContextProvider securityContextProvider,
            IdEncoder idEncoder) {
        this.repository = repository;
        this.securityContextProvider = securityContextProvider;
        this.idEncoder = idEncoder;
    }

    /**
     * Audits one annotated business operation.
     *
     * @param joinPoint intercepted method invocation
     * @param auditable audit metadata
     * @return original method result
     * @throws Throwable when the business operation fails
     */
    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        boolean nested = AuditContext.isActive();
        if (nested && !auditable.alwaysLog()) {
            return joinPoint.proceed();
        }

        boolean root = !nested;
        if (root) {
            AuditContext.start();
        }

        try {
            Object result = joinPoint.proceed();
            writeSuccess(joinPoint, auditable, result);
            return result;
        } catch (Throwable throwable) {
            writeFailure(auditable, throwable);
            throw throwable;
        } finally {
            if (root) {
                AuditContext.clear();
            }
        }
    }

    private void writeSuccess(ProceedingJoinPoint joinPoint, Auditable auditable, Object result) {
        try {
            repository.save(baseLog(auditable)
                    .resourceType(result != null ? result.getClass().getSimpleName() : null)
                    .resourceId(resolveEntityId(result))
                    .description(resolveDescription(auditable.description(), joinPoint, result))
                    .status("SUCCESS")
                    .build());
        } catch (Exception exception) {
            LOG.error("Failed to write successful method audit log", exception);
        }
    }

    private void writeFailure(Auditable auditable, Throwable throwable) {
        try {
            repository.save(baseLog(auditable)
                    .description("Failed: " + failureMessage(throwable))
                    .status("FAILED")
                    .build());
        } catch (Exception exception) {
            LOG.error("Failed to write failed method audit log", exception);
        }
    }

    private AuditLog.AuditLogBuilder<?, ?> baseLog(Auditable auditable) {
        return AuditLog.builder()
                .traceId(MDC.get("traceId"))
                .actorId(securityContextProvider.getCurrentUsername())
                .actorIp(securityContextProvider.getCurrentIp())
                .action(auditable.action())
                .module(auditable.module())
                .createdAt(Instant.now());
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

    private String resolveDescription(String template,
            ProceedingJoinPoint joinPoint,
            Object result) {
        if (template == null || template.isEmpty() || !template.contains("#{")) {
            return template == null || template.isEmpty() ? null : template;
        }

        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            StandardEvaluationContext context = new MethodBasedEvaluationContext(
                    null, method, joinPoint.getArgs(), parameterNameDiscoverer);
            context.setVariable("result", result);

            String resolved = template;
            while (resolved.contains("#{")) {
                int start = resolved.indexOf("#{");
                int end = resolved.indexOf("}", start);
                if (end == -1) {
                    break;
                }

                String expression = resolved.substring(start + 2, end);
                Object value = spelParser.parseExpression(expression).getValue(context);
                resolved = resolved.substring(0, start)
                        + (value != null ? value.toString() : "null")
                        + resolved.substring(end + 1);
            }
            return resolved;
        } catch (Exception exception) {
            LOG.warn("Failed to resolve SpEL in audit description: {}", template, exception);
            return template;
        }
    }

    private String failureMessage(Throwable throwable) {
        String message = throwable.getMessage();
        return message != null && !message.isBlank()
                ? message
                : throwable.getClass().getSimpleName();
    }
}
