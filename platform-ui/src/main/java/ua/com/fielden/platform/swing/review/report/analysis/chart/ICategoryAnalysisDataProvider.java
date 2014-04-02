package ua.com.fielden.platform.swing.review.report.analysis.chart;

import java.util.List;

import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.reportquery.AnalysisModelChangedListener;

/**
 * An contract for anything that provides grouped and aggregated data, can be used in chart and pivot analysis.
 * 
 * @author TG Team
 * 
 * @param <CDT>
 *            - Category data type.
 * @param <ADT>
 *            - Aggregated data type
 * @param <LDT>
 *            - Loaded data type (i.e. IPage or list of {@link EntityAggregates}).
 */
public interface ICategoryAnalysisDataProvider<CDT, ADT, LDT> {

    /**
     * Returns the number of loaded data entries.
     * 
     * @return
     */
    int getCategoryDataEntryCount();

    /**
     * Returns the value of grouped by property in the entry specified with index.
     * 
     * @param index
     *            - the entry index from where the value of grouped by property should be retrieved.
     * @param category
     *            - the name of grouped by property.
     * @return
     */
    CDT getCategoryDataValue(int index, String category);

    /**
     * Returns the value of aggregated property from the entry specified with index.
     * 
     * @param index
     *            - the entry index from where the value of aggregated property should be retrieved.
     * @param aggregated
     *            - the name of the aggregated property.
     * @return
     */
    ADT getAggregatedDataValue(int index, String aggregated);

    /**
     * Returns the loaded data.
     * 
     * @return
     */
    LDT getLoadedData();

    /**
     * Adds the {@link AnalysisModelChangedListener} instance, that listens the analysis data change events.
     * 
     * @param l
     */
    void addAnalysisModelChangedListener(final AnalysisModelChangedListener l);

    /**
     * Removes the {@link AnalysisModelChangedListener} instance.
     * 
     * @param l
     */
    void removeAnalysisModelChangedListener(final AnalysisModelChangedListener l);

    /**
     * Returns the aggregated properties associated with loaded data.
     * 
     * @return
     */
    List<String> aggregatedProperties();

    /**
     * Returns the "grouped by" properties associated with loaded data.
     * 
     * @return
     */
    List<String> categoryProperties();
}
