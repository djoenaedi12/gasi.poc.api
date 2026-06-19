package gasi.gps.core.starter.application.approval;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import gasi.gps.core.api.application.exception.BusinessException;
import gasi.gps.core.api.approval.ApprovalTarget;

/**
 * Registry for approval targets contributed by resource modules.
 *
 * <p>
 * This registry allows the approval workflow engine to resolve the resource
 * service that must execute the final approval result.
 * </p>
 *
 * @since 1.0.0
 */
@Component
public class ApprovalTargetRegistry {

    private final Map<String, ApprovalTarget> targets;

    /**
     * Creates a registry from available approval targets.
     *
     * @param targets approval targets
     */
    public ApprovalTargetRegistry(List<ApprovalTarget> targets) {
        this.targets = targets.stream()
                .collect(Collectors.toMap(
                        ApprovalTarget::resourceType,
                        Function.identity(),
                        (first, second) -> first));
    }

    /**
     * Resolves an approval target by resource type.
     *
     * @param resourceType resource type
     * @return matching approval target
     */
    public ApprovalTarget resolve(String resourceType) {
        ApprovalTarget target = targets.get(resourceType);
        if (target == null) {
            throw new BusinessException("Approval target not found: " + resourceType);
        }
        return target;
    }
}
