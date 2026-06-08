package gasi.gps.dataupload.application.dto;

import gasi.gps.core.api.application.dto.BaseDetailResponse;
import gasi.gps.dataupload.domain.model.UploadRowStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Detail response DTO for a single upload row.
 *
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DataRowUplDetailResponse extends BaseDetailResponse {
    private int rowNumber;
    private String rowData;
    private UploadRowStatus rowStatus;
    private String identifier;
    private String errorMessage;
}
