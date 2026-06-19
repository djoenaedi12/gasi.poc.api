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
import gasi.gps.core.api.application.hook.ResourceMapperHook;
import gasi.gps.core.api.domain.model.BaseModel;

/**
 * Registry for ordered resource mapper hooks.
 *
 * @since 1.0.0
 */
@Component
public class ResourceMapperHookRegistry {

    private final Map<String, List<ResourceMapperHook<?, ?, ?, ?, ?>>> hooksByResource;

    public ResourceMapperHookRegistry(List<ResourceMapperHook<?, ?, ?, ?, ?>> hooks) {
        List<ResourceMapperHook<?, ?, ?, ?, ?>> orderedHooks = new ArrayList<>(hooks);
        AnnotationAwareOrderComparator.sort(orderedHooks);

        Map<String, List<ResourceMapperHook<?, ?, ?, ?, ?>>> grouped = new HashMap<>();
        for (ResourceMapperHook<?, ?, ?, ?, ?> hook : orderedHooks) {
            ResourceHook annotation = AnnotatedElementUtils.findMergedAnnotation(
                    hook.getClass(), ResourceHook.class);
            if (annotation == null || annotation.layer() != HookLayer.MAPPER || annotation.value().isBlank()) {
                continue;
            }
            grouped.computeIfAbsent(annotation.value(), ignored -> new ArrayList<>()).add(hook);
        }
        this.hooksByResource = Map.copyOf(grouped);
    }

    public <D extends BaseModel, CRQ, URQ, SRS, DRS> ResourceMapperHook<D, CRQ, URQ, SRS, DRS> resolve(
            String resourceType) {
        List<ResourceMapperHook<?, ?, ?, ?, ?>> hooks = hooksByResource.get(resourceType);
        if (hooks == null || hooks.isEmpty()) {
            return ResourceMapperHook.noop();
        }

        List<ResourceMapperHook<D, CRQ, URQ, SRS, DRS>> typedHooks = new ArrayList<>();
        for (ResourceMapperHook<?, ?, ?, ?, ?> hook : hooks) {
            typedHooks.add(cast(hook));
        }
        return ResourceMapperHook.composite(typedHooks);
    }

    @SuppressWarnings("unchecked")
    private static <D extends BaseModel, CRQ, URQ, SRS, DRS> ResourceMapperHook<D, CRQ, URQ, SRS, DRS> cast(
            ResourceMapperHook<?, ?, ?, ?, ?> hook) {
        return (ResourceMapperHook<D, CRQ, URQ, SRS, DRS>) hook;
    }
}
