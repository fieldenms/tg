package ua.com.fielden.platform.reportquery;

/**
 * Represents the Category Chart entries. Defines the API for retrieving the categories, and series.
 * 
 * @author oleh
 * 
 */
public interface ICategoryChartEntryModel {

    /**
     * Returns the number of categories (columns) needed to build the category chart.
     * 
     * @return
     */
    int getCategoryCount();

    /**
     * Returns the number of series (rows) needed to build the category chart
     * 
     * @return
     */
    int getSeriesCount();

    /**
     * Returns the series (line) for the specified index
     * 
     * @param index
     *            - the index of the series (row)
     * @return
     */
    Comparable<?> getSeries(final int index);

    /**
     * Returns the category (column) for the specified index
     * 
     * @param index
     *            - the index of the category (column)
     * @return
     */
    Comparable<?> getCategory(final int index);

    /**
     * Returns the value for the specified series (row) and category (column) index
     * 
     * @param row
     *            - the series index
     * @param column
     *            - the category index
     * @return
     */
    Number getValue(final int row, final int column);

    /**
     * Adds the {@link AnalysisModelChangedListener} instance, that listens the chart model change events.
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
}
