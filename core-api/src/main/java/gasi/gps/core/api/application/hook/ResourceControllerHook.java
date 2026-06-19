package gasi.gps.core.api.application.hook;

import java.util.List;

import gasi.gps.core.api.domain.model.PageResult;
import gasi.gps.core.api.presentation.dto.ApiResponse;
import gasi.gps.core.api.presentation.dto.SearchRequest;

/**
 * Lifecycle hook contract for resource controller operations.
 *
 * @param <CRQ> create request DTO type
 * @param <URQ> update request DTO type
 * @param <SRS> summary response DTO type
 * @param <DRS> detail response DTO type
 * @since 1.0.0
 */
public interface ResourceControllerHook<CRQ, URQ, SRS, DRS> {

    default void beforeFindByIdRequest(String id) {
    }

    default void afterFindByIdResponse(ApiResponse<DRS> response, String id) {
    }

    default void beforeFindByRequest(SearchRequest request) {
    }

    default void afterFindByResponse(ApiResponse<DRS> response, SearchRequest request) {
    }

    default void beforeFindAllRequest(SearchRequest request) {
    }

    default void afterFindAllResponse(ApiResponse<List<?>> response, SearchRequest request) {
    }

    default void beforeFindAllPagedRequest(SearchRequest request) {
    }

    default void afterFindAllPagedResponse(ApiResponse<PageResult<?>> response, SearchRequest request) {
    }

    default void beforeLookupPagedRequest(SearchRequest request) {
    }

    default void afterLookupPagedResponse(ApiResponse<PageResult<?>> response, SearchRequest request) {
    }

    default void beforeCreateRequest(CRQ request) {
    }

    default void afterCreateResponse(ApiResponse<DRS> response, CRQ request) {
    }

    default void beforeUpdateRequest(String id, URQ request) {
    }

    default void afterUpdateResponse(ApiResponse<DRS> response, String id, URQ request) {
    }

    default void beforeDeleteRequest(String id) {
    }

    default void afterDeleteResponse(ApiResponse<Void> response, String id) {
    }

    static <CRQ, URQ, SRS, DRS> ResourceControllerHook<CRQ, URQ, SRS, DRS> noop() {
        return new ResourceControllerHook<>() {
        };
    }

    static <CRQ, URQ, SRS, DRS> ResourceControllerHook<CRQ, URQ, SRS, DRS> composite(
            List<? extends ResourceControllerHook<CRQ, URQ, SRS, DRS>> hooks) {
        if (hooks == null || hooks.isEmpty()) {
            return noop();
        }
        return new ResourceControllerHook<>() {
            @Override
            public void beforeFindByIdRequest(String id) {
                hooks.forEach(hook -> hook.beforeFindByIdRequest(id));
            }

            @Override
            public void afterFindByIdResponse(ApiResponse<DRS> response, String id) {
                hooks.forEach(hook -> hook.afterFindByIdResponse(response, id));
            }

            @Override
            public void beforeFindByRequest(SearchRequest request) {
                hooks.forEach(hook -> hook.beforeFindByRequest(request));
            }

            @Override
            public void afterFindByResponse(ApiResponse<DRS> response, SearchRequest request) {
                hooks.forEach(hook -> hook.afterFindByResponse(response, request));
            }

            @Override
            public void beforeFindAllRequest(SearchRequest request) {
                hooks.forEach(hook -> hook.beforeFindAllRequest(request));
            }

            @Override
            public void afterFindAllResponse(ApiResponse<List<?>> response, SearchRequest request) {
                hooks.forEach(hook -> hook.afterFindAllResponse(response, request));
            }

            @Override
            public void beforeFindAllPagedRequest(SearchRequest request) {
                hooks.forEach(hook -> hook.beforeFindAllPagedRequest(request));
            }

            @Override
            public void afterFindAllPagedResponse(ApiResponse<PageResult<?>> response, SearchRequest request) {
                hooks.forEach(hook -> hook.afterFindAllPagedResponse(response, request));
            }

            @Override
            public void beforeLookupPagedRequest(SearchRequest request) {
                hooks.forEach(hook -> hook.beforeLookupPagedRequest(request));
            }

            @Override
            public void afterLookupPagedResponse(ApiResponse<PageResult<?>> response, SearchRequest request) {
                hooks.forEach(hook -> hook.afterLookupPagedResponse(response, request));
            }

            @Override
            public void beforeCreateRequest(CRQ request) {
                hooks.forEach(hook -> hook.beforeCreateRequest(request));
            }

            @Override
            public void afterCreateResponse(ApiResponse<DRS> response, CRQ request) {
                hooks.forEach(hook -> hook.afterCreateResponse(response, request));
            }

            @Override
            public void beforeUpdateRequest(String id, URQ request) {
                hooks.forEach(hook -> hook.beforeUpdateRequest(id, request));
            }

            @Override
            public void afterUpdateResponse(ApiResponse<DRS> response, String id, URQ request) {
                hooks.forEach(hook -> hook.afterUpdateResponse(response, id, request));
            }

            @Override
            public void beforeDeleteRequest(String id) {
                hooks.forEach(hook -> hook.beforeDeleteRequest(id));
            }

            @Override
            public void afterDeleteResponse(ApiResponse<Void> response, String id) {
                hooks.forEach(hook -> hook.afterDeleteResponse(response, id));
            }
        };
    }
}
