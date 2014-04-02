package ua.com.fielden.platform.ndimcube;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Structure that stores data for {@link EntitiesMultipleDimensionCubeModel}.
 * 
 * @author TG Team
 * 
 * @param <T>
 */
public class EntitiesMultipleDimensionCubeData<T extends AbstractEntity<?>> {

    private final Map<List<String>, List<T>> data = new HashMap<>();
    private final List<String> rowDistributionProperties = new ArrayList<>();
    private final List<String> columnDistributionProperties = new ArrayList<>();
    private final List<String> aggregationProperties = new ArrayList<>();
    private final Map<String, String> columnNames = new HashMap<>();
    private final Map<String, String> columnToolTips = new HashMap<>();
    private final Map<String, Class<?>> columnClass = new HashMap<>();

    /**
     * Returns the data for {@link EntitiesMultipleDimensionCubeModel}.
     * 
     * @return
     */
    public Map<List<String>, List<T>> getData() {
        return Collections.unmodifiableMap(data);
    }

    /**
     * Set the data for {@link EntitiesMultipleDimensionCubeModel}.
     * 
     * @param data
     * @return
     */
    public EntitiesMultipleDimensionCubeData<T> setData(final Map<List<String>, List<T>> data) {
        this.data.clear();
        if (data != null) {
            this.data.putAll(data);
        }
        return this;
    }

    /**
     * Returns the list of distribution properties shown in the row.
     * 
     * @return
     */
    public List<String> getRowDistributionProperties() {
        return Collections.unmodifiableList(rowDistributionProperties);
    }

    /**
     * Set the list of distribution properties shown in the row.
     * 
     * @param rowDistributionProperties
     * @return
     */
    public EntitiesMultipleDimensionCubeData<T> setRowDistributionProperties(final List<String> rowDistributionProperties) {
        this.rowDistributionProperties.clear();
        if (rowDistributionProperties != null) {
            this.rowDistributionProperties.addAll(rowDistributionProperties);
        }
        return this;
    }

    /**
     * Returns the list of distribution properties shown in column.
     * 
     * @return
     */
    public List<String> getColumnDistributionProperties() {
        return Collections.unmodifiableList(columnDistributionProperties);
    }

    /**
     * Set the list of distribution properties shown in column.
     * 
     * @param columnDistributionProperties
     * @return
     */
    public EntitiesMultipleDimensionCubeData<T> setColumnDistributionProperties(final List<String> columnDistributionProperties) {
        this.columnDistributionProperties.clear();
        if (columnDistributionProperties != null) {
            this.columnDistributionProperties.addAll(columnDistributionProperties);
        }
        return this;
    }

    /**
     * Returns the list of aggregation properties.
     * 
     * @return
     */
    public List<String> getAggregationProperties() {
        return Collections.unmodifiableList(aggregationProperties);
    }

    /**
     * Set the list of aggregation properties.
     * 
     * @param aggregationProperties
     * @return
     */
    public EntitiesMultipleDimensionCubeData<T> setAggregationProperties(final List<String> aggregationProperties) {
        this.aggregationProperties.clear();
        if (aggregationProperties != null) {
            this.aggregationProperties.addAll(aggregationProperties);
        }
        return this;
    }

    /**
     * Returns the map between column identifiers and column names.
     * 
     * @return
     */
    public Map<String, String> getColumnNames() {
        return Collections.unmodifiableMap(columnNames);
    }

    /**
     * Set the map between column identifiers and column names.
     * 
     * @param columnNames
     * @return
     */
    public EntitiesMultipleDimensionCubeData<T> setColumnNames(final Map<String, String> columnNames) {
        this.columnNames.clear();
        if (columnNames != null) {
            this.columnNames.putAll(columnNames);
        }
        return this;
    }

    /**
     * Returns the map between column identifiers and column tool tips.
     * 
     * @return
     */
    public Map<String, String> getColumnToolTips() {
        return Collections.unmodifiableMap(columnToolTips);
    }

    public EntitiesMultipleDimensionCubeData<T> setColumnToolTips(final Map<String, String> columnToolTips) {
        this.columnToolTips.clear();
        if (columnToolTips != null) {
            this.columnToolTips.putAll(columnToolTips);
        }
        return this;
    }

    /**
     * Returns the map between column identifiers and column class.
     * 
     * @return
     */
    public Map<String, Class<?>> getColumnClass() {
        return Collections.unmodifiableMap(columnClass);
    }

    /**
     * Set the map between column identifiers and column class.
     * 
     * @param columnClass
     * @return
     */
    public EntitiesMultipleDimensionCubeData<T> setColumnClass(final Map<String, Class<?>> columnClass) {
        this.columnClass.clear();
        if (columnClass != null) {
            this.columnClass.putAll(columnClass);
        }
        return this;
    }
}
