package ua.com.fielden.platform.swing.review.report.analysis.chart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reportquery.AnalysisModelChangedEvent;
import ua.com.fielden.platform.types.Money;

public class GroupAnalysisDataProvider<T extends AbstractEntity<?>> extends AbstractCategoryAnalysisDataProvider<Comparable<?>, Number, List<T>> {

    //Category properties and it's aliases.
    private final List<String> categoryList = new ArrayList<>();
    private final List<String> aggregationList = new ArrayList<>();

    private final List<T> loadedData = new ArrayList<>();

    @Override
    public int getCategoryDataEntryCount() {
	return loadedData.size();
    }

    @Override
    public Comparable<?> getCategoryDataValue(final int index, final String category) {
	return (Comparable<?>) loadedData.get(index).get(category);
    }

    @Override
    public Number getAggregatedDataValue(final int index, final String aggregated) {
	final Object value = loadedData.get(index).get(aggregated);
	if (value == null) {
	    return 0;
	} else if (value instanceof Money) {
	    return ((Money) value).getAmount();
	} else if (value instanceof Number) {
	    return (Number) value;
	}
	throw new IllegalArgumentException("The value type is " + value.getClass().getSimpleName() + " please make sure that the passed parameters are correct\n"
		+ "The index is: " + index + ". The aggregation property is: " + aggregated + ".");
    }

    @Override
    public List<T> getLoadedData() {
	return Collections.unmodifiableList(loadedData);
    }

    /**
     * Set the loaded page and data.
     *
     * @param loadedData
     */
    public void setLoadedData(final List<T> loadedData) {
	if (loadedData != null) {
	    this.loadedData.clear();
	    this.loadedData.addAll(loadedData);
	    fireAnalysisModelChangeEvent(new AnalysisModelChangedEvent(this));
	}
    }

    /**
     * Set the map that associates the aggregated and grouped by properties with their aliases.
     *
     * @param aliasMap
     */
    public void setUsedProperties(final List<String> categoryList, final List<String> aggregationList) {
	if (categoryList == null || aggregationList == null) {
	    return;
	}
	this.categoryList.clear();
	this.categoryList.addAll(categoryList);
	this.aggregationList.clear();
	this.aggregationList.addAll(aggregationList);
    }

    @Override
    public List<String> aggregatedProperties() {
	return Collections.unmodifiableList(aggregationList);
    }

    @Override
    public List<String> categoryProperties() {
	return Collections.unmodifiableList(categoryList);
    }
}