package gasi.gps.dataupload.application.dto;

import gasi.gps.core.api.application.dto.BaseSummaryResponse;
import gasi.gps.dataupload.domain.model.UploadStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Summary response DTO for upload batch listing.
 *
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DataUplSummaryResponse extends BaseSummaryResponse {
    private String instructionNo;
    private String fileName;
    private int totalRows;
    private int validRows;
    private int invalidRows;
    private UploadStatus uploadStatus;
}
