package gasi.gps.core.starter.application.customfield;

import java.util.Map;

import org.springframework.stereotype.Component;

import gasi.gps.core.api.application.dto.BaseDetailResponse;
import gasi.gps.core.api.application.dto.BaseRequest;
import gasi.gps.core.api.application.hook.ResourceServiceHook;
import gasi.gps.core.api.customfield.CustomFieldContext;
import gasi.gps.core.api.domain.model.BaseModel;

/**
 * Resource service hook for custom field values.
 *
 * <p>
 * This hook stores submitted custom field values after create or update
 * operations, and attaches custom field values to detail responses.
 * </p>
 *
 * @param <D>   domain model type
 * @param <CRQ> create request DTO
 * @param <URQ> update request DTO
 * @param <SRS> summary response DTO
 * @param <DRS> detail response DTO
 * @since 1.0.0
 */
@Component
public class CustomFieldResourceServiceHook<D extends BaseModel, CRQ, URQ, SRS, DRS>
        implements ResourceServiceHook<D, CRQ, URQ, SRS, DRS> {

    private final CustomFieldExtensionRegistry customFieldExtensionRegistry;

    /**
     * Creates a custom field resource service hook.
     *
     * @param customFieldExtensionRegistry custom field extension registry
     */
    public CustomFieldResourceServiceHook(CustomFieldExtensionRegistry customFieldExtensionRegistry) {
        this.customFieldExtensionRegistry = customFieldExtensionRegistry;
    }

    @Override
    public void afterCreate(String resourceType, D saved, CRQ request) {
        replaceValues(resourceType, saved, request);
    }

    @Override
    public void afterUpdate(String resourceType, D saved, URQ request) {
        replaceValues(resourceType, saved, request);
    }

    @Override
    public void afterCreateResponse(String resourceType, DRS response, D saved, CRQ request) {
        attachValues(resourceType, response, saved);
    }

    @Override
    public void afterUpdateResponse(String resourceType, DRS response, D saved, URQ request) {
        attachValues(resourceType, response, saved);
    }

    private void replaceValues(String resourceType, D saved, Object request) {
        if (!(request instanceof BaseRequest baseRequest)) {
            return;
        }

        Map<String, Object> customFields = baseRequest.getCustomFields();

        if (customFields == null || customFields.isEmpty()) {
            return;
        }

        CustomFieldContext context = new CustomFieldContext(
                Map.of(),
                customFields);

        customFieldExtensionRegistry.replaceValues(
                resourceType,
                saved.getId(),
                customFields,
                context);
    }

    private void attachValues(String resourceType, Object response, D saved) {
        if (!(response instanceof BaseDetailResponse detailResponse)) {
            return;
        }

        detailResponse.setCustomFields(
                customFieldExtensionRegistry.getValues(resourceType, saved.getId()));
    }
}
