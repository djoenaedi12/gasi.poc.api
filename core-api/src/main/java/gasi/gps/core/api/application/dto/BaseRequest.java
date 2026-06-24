package gasi.gps.core.api.application.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base request DTO for mutation operations.
 *
 * <p>
 * Provides optional custom field values for resources that support custom
 * fields. Resources that do not use custom fields may leave this value empty.
 * </p>
 *
 * @since 1.0.0
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseRequest {

    /** Optional custom field values keyed by field code. */
    @Builder.Default
    private Map<String, Object> customFields = Map.of();
}
