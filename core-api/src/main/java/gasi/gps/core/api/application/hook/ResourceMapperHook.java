package gasi.gps.core.api.application.hook;

import gasi.gps.core.api.domain.model.BaseModel;

/**
 * Lifecycle hook contract for resource mapper operations.
 *
 * @param <D>   domain model type
 * @param <CRQ> create request DTO type
 * @param <URQ> update request DTO type
 * @param <SRS> summary response DTO type
 * @param <DRS> detail response DTO type
 * @since 1.0.0
 */
public interface ResourceMapperHook<D extends BaseModel, CRQ, URQ, SRS, DRS> {

    default void afterToCreateDomain(D domain, CRQ request) {
    }

    default void afterToUpdateDomain(D domain, URQ request) {
    }

    default void afterUpdateDomain(D domain, URQ request) {
    }

    default void afterToSummary(SRS response, D domain) {
    }

    default void afterToDetail(DRS response, D domain) {
    }

    static <D extends BaseModel, CRQ, URQ, SRS, DRS> ResourceMapperHook<D, CRQ, URQ, SRS, DRS> noop() {
        return new ResourceMapperHook<>() {
        };
    }

    static <D extends BaseModel, CRQ, URQ, SRS, DRS> ResourceMapperHook<D, CRQ, URQ, SRS, DRS> composite(
            java.util.List<? extends ResourceMapperHook<D, CRQ, URQ, SRS, DRS>> hooks) {
        if (hooks == null || hooks.isEmpty()) {
            return noop();
        }
        return new ResourceMapperHook<>() {
            @Override
            public void afterToCreateDomain(D domain, CRQ request) {
                hooks.forEach(hook -> hook.afterToCreateDomain(domain, request));
            }

            @Override
            public void afterToUpdateDomain(D domain, URQ request) {
                hooks.forEach(hook -> hook.afterToUpdateDomain(domain, request));
            }

            @Override
            public void afterUpdateDomain(D domain, URQ request) {
                hooks.forEach(hook -> hook.afterUpdateDomain(domain, request));
            }

            @Override
            public void afterToSummary(SRS response, D domain) {
                hooks.forEach(hook -> hook.afterToSummary(response, domain));
            }

            @Override
            public void afterToDetail(DRS response, D domain) {
                hooks.forEach(hook -> hook.afterToDetail(response, domain));
            }
        };
    }
}
