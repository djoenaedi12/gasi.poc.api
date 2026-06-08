package gasi.gps.dataupload.domain.model;

import java.util.List;

/**
 * Column definition for an upload template.
 *
 * @param key       stable column key consumed by parsers
 * @param label     displayed column header
 * @param required  whether the value is mandatory
 * @param type      logical data type
 * @param comment   short header comment
 * @param guideline longer guideline text
 * @param options   static dropdown options
 * @param lookup    dynamic lookup/reference metadata
 * @since 1.0.0
 */
public record DataUplTemplateColumn(
        String key,
        String label,
        boolean required,
        DataUplTemplateColumnType type,
        String comment,
        String guideline,
        List<DataUplTemplateOption> options,
        DataUplTemplateLookup lookup) {

    /**
     * Creates a template column and normalizes nullable type/options values.
     */
    public DataUplTemplateColumn {
        type = type == null ? DataUplTemplateColumnType.TEXT : type;
        options = options == null ? List.of() : List.copyOf(options);
    }

    /**
     * Creates an optional text column.
     *
     * @param key   stable column key consumed by parsers
     * @param label displayed column header
     * @return text template column
     */
    public static DataUplTemplateColumn text(String key, String label) {
        return text(key, label, false, null, null);
    }

    /**
     * Creates a text column.
     *
     * @param key       stable column key consumed by parsers
     * @param label     displayed column header
     * @param required  whether the value is mandatory
     * @param comment   short header comment
     * @param guideline longer guideline text
     * @return text template column
     */
    public static DataUplTemplateColumn text(
            String key,
            String label,
            boolean required,
            String comment,
            String guideline) {
        return new DataUplTemplateColumn(
                key, label, required, DataUplTemplateColumnType.TEXT, comment, guideline, List.of(), null);
    }

    /**
     * Creates a numeric column.
     *
     * @param key       stable column key consumed by parsers
     * @param label     displayed column header
     * @param required  whether the value is mandatory
     * @param comment   short header comment
     * @param guideline longer guideline text
     * @return numeric template column
     */
    public static DataUplTemplateColumn number(
            String key,
            String label,
            boolean required,
            String comment,
            String guideline) {
        return new DataUplTemplateColumn(
                key, label, required, DataUplTemplateColumnType.NUMBER, comment, guideline, List.of(), null);
    }

    /**
     * Creates a date column.
     *
     * @param key       stable column key consumed by parsers
     * @param label     displayed column header
     * @param required  whether the value is mandatory
     * @param comment   short header comment
     * @param guideline longer guideline text
     * @return date template column
     */
    public static DataUplTemplateColumn date(
            String key,
            String label,
            boolean required,
            String comment,
            String guideline) {
        return new DataUplTemplateColumn(
                key, label, required, DataUplTemplateColumnType.DATE, comment, guideline, List.of(), null);
    }

    /**
     * Creates a static options column.
     *
     * @param key       stable column key consumed by parsers
     * @param label     displayed column header
     * @param required  whether the value is mandatory
     * @param options   selectable options
     * @param comment   short header comment
     * @param guideline longer guideline text
     * @return static options template column
     */
    public static DataUplTemplateColumn options(
            String key,
            String label,
            boolean required,
            List<DataUplTemplateOption> options,
            String comment,
            String guideline) {
        return new DataUplTemplateColumn(
                key, label, required, DataUplTemplateColumnType.ENUM, comment, guideline, options, null);
    }

    /**
     * Creates a dynamic lookup column.
     *
     * @param key       stable column key consumed by parsers
     * @param label     displayed column header
     * @param required  whether the value is mandatory
     * @param lookup    lookup metadata
     * @param comment   short header comment
     * @param guideline longer guideline text
     * @return lookup template column
     */
    public static DataUplTemplateColumn lookup(
            String key,
            String label,
            boolean required,
            DataUplTemplateLookup lookup,
            String comment,
            String guideline) {
        return new DataUplTemplateColumn(
                key, label, required, DataUplTemplateColumnType.LOOKUP, comment, guideline, List.of(), lookup);
    }
}
