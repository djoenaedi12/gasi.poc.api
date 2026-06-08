package gasi.gps.dataupload.domain.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Resource-specific upload template definition.
 *
 * @param fileName           suggested downloaded file name
 * @param mainSheetName      main data sheet name for future spreadsheet renderers
 * @param guidelineSheetName guideline sheet name for future spreadsheet renderers
 * @param columns            ordered data columns
 * @since 1.0.0
 */
public record DataUplTemplateSpec(
        String fileName,
        String mainSheetName,
        String guidelineSheetName,
        List<DataUplTemplateColumn> columns) {

    /**
     * Creates a template specification and normalizes blank sheet names and
     * nullable columns.
     */
    public DataUplTemplateSpec {
        mainSheetName = mainSheetName == null || mainSheetName.isBlank() ? "Data" : mainSheetName;
        guidelineSheetName = guidelineSheetName == null || guidelineSheetName.isBlank()
                ? "Guideline"
                : guidelineSheetName;
        columns = columns == null ? List.of() : List.copyOf(columns);
    }

    /**
     * Starts a template specification builder.
     *
     * @param fileName suggested downloaded file name
     * @return template specification builder
     */
    public static Builder builder(String fileName) {
        return new Builder(fileName);
    }

    /**
     * Builder for {@link DataUplTemplateSpec}.
     *
     * @since 1.0.0
     */
    public static final class Builder {
        private final String fileName;
        private String mainSheetName = "Data";
        private String guidelineSheetName = "Guideline";
        private final List<DataUplTemplateColumn> columns = new ArrayList<>();

        private Builder(String fileName) {
            this.fileName = fileName;
        }

        /**
         * Sets the main data sheet name.
         *
         * @param value sheet name
         * @return this builder
         */
        public Builder mainSheetName(String value) {
            this.mainSheetName = value;
            return this;
        }

        /**
         * Sets the guideline sheet name.
         *
         * @param value sheet name
         * @return this builder
         */
        public Builder guidelineSheetName(String value) {
            this.guidelineSheetName = value;
            return this;
        }

        /**
         * Adds one template column.
         *
         * @param column column to add
         * @return this builder
         */
        public Builder column(DataUplTemplateColumn column) {
            this.columns.add(column);
            return this;
        }

        /**
         * Adds multiple template columns.
         *
         * @param values columns to add
         * @return this builder
         */
        public Builder columns(List<DataUplTemplateColumn> values) {
            this.columns.addAll(values);
            return this;
        }

        /**
         * Builds the template specification.
         *
         * @return template specification
         */
        public DataUplTemplateSpec build() {
            return new DataUplTemplateSpec(fileName, mainSheetName, guidelineSheetName, columns);
        }
    }
}
