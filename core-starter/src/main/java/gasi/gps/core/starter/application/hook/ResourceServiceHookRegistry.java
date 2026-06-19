package gasi.gps.core.starter.application.hook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

import gasi.gps.core.api.application.hook.HookLayer;
import gasi.gps.core.api.application.hook.ResourceHook;
import gasi.gps.core.api.application.hook.ResourceServiceHook;
import gasi.gps.core.api.domain.model.BaseModel;

/**
 * Registry for ordered resource service hooks.
 *
 * <p>
 * Hooks are discovered as Spring beans and included only when annotated with
 * {@link ResourceHook} for the {@link HookLayer#SERVICE} layer. Multiple hooks
 * for the same resource are executed according to Spring's {@code @Order}
 * semantics.
 * </p>
 *
 * @since 1.0.0
 */
@Component
public class ResourceServiceHookRegistry {

    private final Map<String, List<ResourceServiceHook<?, ?, ?, ?, ?>>> hooksByResource;

    /**
     * Creates a hook registry from all hook beans in the application context.
     *
     * @param hooks discovered hook beans
     */
    public ResourceServiceHookRegistry(List<ResourceServiceHook<?, ?, ?, ?, ?>> hooks) {
        List<ResourceServiceHook<?, ?, ?, ?, ?>> orderedHooks = new ArrayList<>(hooks);
        AnnotationAwareOrderComparator.sort(orderedHooks);

        Map<String, List<ResourceServiceHook<?, ?, ?, ?, ?>>> grouped = new HashMap<>();
        for (ResourceServiceHook<?, ?, ?, ?, ?> hook : orderedHooks) {
            ResourceHook annotation = AnnotatedElementUtils.findMergedAnnotation(
                    hook.getClass(), ResourceHook.class);
            if (annotation == null || annotation.layer() != HookLayer.SERVICE || annotation.value().isBlank()) {
                continue;
            }
            grouped.computeIfAbsent(annotation.value(), ignored -> new ArrayList<>()).add(hook);
        }
        this.hooksByResource = Map.copyOf(grouped);
    }

    /**
     * Resolves a composite hook for a resource type.
     *
     * @param resourceType resource type name
     * @param <D>          domain model type
     * @param <CRQ>        create request DTO type
     * @param <URQ>        update request DTO type
     * @param <SRS>        summary response DTO type
     * @param <DRS>        detail response DTO type
     * @return ordered composite hook, or no-op if none is registered
     */
    public <D extends BaseModel, CRQ, URQ, SRS, DRS> ResourceServiceHook<D, CRQ, URQ, SRS, DRS> resolve(
            String resourceType) {
        List<ResourceServiceHook<?, ?, ?, ?, ?>> hooks = hooksByResource.get(resourceType);
        if (hooks == null || hooks.isEmpty()) {
            return ResourceServiceHook.noop();
        }

        List<ResourceServiceHook<D, CRQ, URQ, SRS, DRS>> typedHooks = new ArrayList<>();
        for (ResourceServiceHook<?, ?, ?, ?, ?> hook : hooks) {
            typedHooks.add(cast(hook));
        }
        return ResourceServiceHook.composite(typedHooks);
    }

    @SuppressWarnings("unchecked")
    private static <D extends BaseModel, CRQ, URQ, SRS, DRS> ResourceServiceHook<D, CRQ, URQ, SRS, DRS> cast(
            ResourceServiceHook<?, ?, ?, ?, ?> hook) {
        return (ResourceServiceHook<D, CRQ, URQ, SRS, DRS>) hook;
    }
}
