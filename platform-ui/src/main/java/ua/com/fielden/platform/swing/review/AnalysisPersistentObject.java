package ua.com.fielden.platform.swing.review;

import java.util.Collections;
import java.util.List;

import javax.swing.SortOrder;

import ua.com.fielden.platform.reportquery.IAggregatedProperty;
import ua.com.fielden.platform.reportquery.IDistributedProperty;
import ua.com.fielden.platform.swing.analysis.AbstractAnalysisPersistentObject;
import ua.com.fielden.platform.swing.analysis.AnalysisReportType;
import ua.com.fielden.platform.swing.analysis.IAnalysisReportPersistentObject;
import ua.com.fielden.platform.swing.analysis.IAnalysisReportType;
import ua.com.fielden.platform.utils.Pair;

/**
 * Object that stores all information about analysis reports (charts) and can be use to save it in to the file or restore from file.
 * 
 * @author oleh
 * 
 */
public class AnalysisPersistentObject extends AbstractAnalysisPersistentObject {

    public final static int DEFAULT_CATEGORY_COUNT = 0;

    //Properties those represent the analysis report. It should be persisted.
    private final IDistributedProperty selectedDistributionProperty;
    private final List<IAggregatedProperty> selectedAggregationProperties;
    private final Pair<IAggregatedProperty, SortOrder> orderedProeprty;
    private final List<IDistributedProperty> availableDistributionProperties;
    private final List<IAggregatedProperty> availableAggregationProperties;
    private final int visibleCategoriesCount;

    protected AnalysisPersistentObject() {
	this.selectedDistributionProperty = null;
	this.selectedAggregationProperties = null;
	this.orderedProeprty = null;
	this.availableDistributionProperties = null;
	this.availableAggregationProperties = null;
	this.visibleCategoriesCount = DEFAULT_CATEGORY_COUNT;
    }

    /**
     * Creates new {@link AnalysisPersistentObject} with name, distribution property and aggregations.
     * 
     * @param distributionTreePath
     * @param aggregationProperties
     */
    public AnalysisPersistentObject(final IDistributedProperty selectedDistributionProperty, final List<IAggregatedProperty> selectedAggregationProperties, final Pair<IAggregatedProperty, SortOrder> orderedProeprty, final List<IDistributedProperty> availableDistributionProperties, final List<IAggregatedProperty> availableAggregationProperties, final int visibleCategoriesCount, final boolean visible) {
	super(visible);
	this.selectedDistributionProperty = selectedDistributionProperty;
	this.selectedAggregationProperties = selectedAggregationProperties;
	this.orderedProeprty = orderedProeprty;
	this.availableDistributionProperties = availableDistributionProperties;
	this.availableAggregationProperties = availableAggregationProperties;
	this.visibleCategoriesCount = visibleCategoriesCount;
    }

    public IDistributedProperty getSelectedDistributionProperty() {
	return selectedDistributionProperty;
    }

    public List<IAggregatedProperty> getSelectedAggregationProperties() {
	return Collections.unmodifiableList(selectedAggregationProperties);
    }

    public Pair<IAggregatedProperty, SortOrder> getOrderedProeprty() {
	return new Pair<IAggregatedProperty, SortOrder>(orderedProeprty.getKey(), orderedProeprty.getValue());
    }

    public List<IDistributedProperty> getAvailableDistributionProperties() {
	return Collections.unmodifiableList(availableDistributionProperties);
    }

    public List<IAggregatedProperty> getAvailableAggregationProperties() {
	return Collections.unmodifiableList(availableAggregationProperties);
    }

    /**
     * Returns the number of categories to be shown at once.
     * 
     * @return
     */
    public int getVisibleCategoriesCount() {
	if (visibleCategoriesCount < 0) {
	    return DEFAULT_CATEGORY_COUNT;
	}
	return visibleCategoriesCount;
    }

    /**
     * Returns true if this {@link AnalysisPersistentObject} is the same as the given one, otherwise it returns false.
     * 
     * @param analysisPersistentObject
     * @return
     */
    public boolean isIdentical(final IAnalysisReportPersistentObject analysisReportPersistentObject) {
	if (this == analysisReportPersistentObject) {
	    return true;
	}
	if (!AnalysisPersistentObject.class.isAssignableFrom(analysisReportPersistentObject.getClass())) {
	    return false;
	}
	final AnalysisPersistentObject analysisPersistentObject = (AnalysisPersistentObject) analysisReportPersistentObject;
	if (getSelectedDistributionProperty() == null && getSelectedDistributionProperty() != analysisPersistentObject.getSelectedDistributionProperty()
		|| getSelectedDistributionProperty() != null && !getSelectedDistributionProperty().equals(analysisPersistentObject.getSelectedDistributionProperty())) {
	    return false;
	}
	if (getSelectedAggregationProperties() == null && getSelectedAggregationProperties() != analysisPersistentObject.getSelectedAggregationProperties()
		|| getSelectedAggregationProperties() != null && !getSelectedAggregationProperties().equals(analysisPersistentObject.getSelectedAggregationProperties())) {
	    return false;
	}
	if (getOrderedProeprty() == null && getOrderedProeprty() != analysisPersistentObject.getOrderedProeprty() || getOrderedProeprty() != null
		&& !getOrderedProeprty().equals(analysisPersistentObject.getOrderedProeprty())) {
	    return false;
	}
	if (getAvailableDistributionProperties() == null && getAvailableDistributionProperties() != analysisPersistentObject.getAvailableDistributionProperties()
		|| getAvailableDistributionProperties() != null && !getAvailableDistributionProperties().equals(analysisPersistentObject.getAvailableDistributionProperties())) {
	    return false;
	}
	if (getAvailableAggregationProperties() == null && getAvailableAggregationProperties() != analysisPersistentObject.getAvailableAggregationProperties()
		|| getAvailableAggregationProperties() != null && !getAvailableAggregationProperties().equals(analysisPersistentObject.getAvailableAggregationProperties())) {
	    return false;
	}
	if (getVisibleCategoriesCount() != analysisPersistentObject.getVisibleCategoriesCount()) {
	    return false;
	}
	return true;
    }

    @Override
    public IAnalysisReportType getType() {
	return AnalysisReportType.ANALYSIS;
    }

}