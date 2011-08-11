package ua.com.fielden.platform.swing.pivot.analysis.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ua.com.fielden.platform.reportquery.IAggregatedProperty;
import ua.com.fielden.platform.reportquery.IDistributedProperty;
import ua.com.fielden.platform.swing.analysis.AbstractAnalysisPersistentObject;
import ua.com.fielden.platform.swing.analysis.AnalysisReportType;
import ua.com.fielden.platform.swing.analysis.IAnalysisReportPersistentObject;
import ua.com.fielden.platform.swing.analysis.IAnalysisReportType;
import ua.com.fielden.platform.swing.checkboxlist.SortObject;
import ua.com.fielden.platform.utils.Pair;

public class PivotAnalysisPersistentObject extends AbstractAnalysisPersistentObject {

    private final int distributionColumnWidth;
    private final List<IDistributedProperty> availableDistributionProperties = new ArrayList<IDistributedProperty>();
    private final List<IAggregatedProperty> availableAggregationProperties = new ArrayList<IAggregatedProperty>();
    private final List<IDistributedProperty> selectedDistributionProperties = new ArrayList<IDistributedProperty>();
    private final List<Pair<IAggregatedProperty, Integer>> selectedAggregationProperties = new ArrayList<Pair<IAggregatedProperty, Integer>>();
    private final List<SortObject<IAggregatedProperty>> sortedAggregations = new ArrayList<SortObject<IAggregatedProperty>>();
    private final Set<IAggregatedProperty> sortableAggregations = new HashSet<IAggregatedProperty>();

    protected PivotAnalysisPersistentObject() {
	this.distributionColumnWidth = 0;
    }

    public PivotAnalysisPersistentObject(final List<IDistributedProperty> availableDistributionProperties, final List<IAggregatedProperty> availableAggregationProperties, final List<IDistributedProperty> selectedDistributionProperties, final List<Pair<IAggregatedProperty, Integer>> selectedAggregationProperties, final List<SortObject<IAggregatedProperty>> sortedAggregations, final Set<IAggregatedProperty> sortableAggregations, final int groupColumnWidth) {
	this.availableDistributionProperties.addAll(availableDistributionProperties != null ? availableDistributionProperties : new ArrayList<IDistributedProperty>());
	this.availableAggregationProperties.addAll(availableAggregationProperties != null ? availableAggregationProperties : new ArrayList<IAggregatedProperty>());
	this.selectedDistributionProperties.addAll(selectedDistributionProperties != null ? selectedDistributionProperties : new ArrayList<IDistributedProperty>());
	this.selectedAggregationProperties.addAll(selectedAggregationProperties != null ? selectedAggregationProperties : new ArrayList<Pair<IAggregatedProperty, Integer>>());
	this.sortedAggregations.addAll(sortedAggregations != null ? sortedAggregations : new ArrayList<SortObject<IAggregatedProperty>>());
	this.sortableAggregations.addAll(sortableAggregations != null ? sortableAggregations : new HashSet<IAggregatedProperty>());
	this.distributionColumnWidth = groupColumnWidth;
    }

    @Override
    public boolean isIdentical(final IAnalysisReportPersistentObject analysisPersistentObject) {
	if (this == analysisPersistentObject) {
	    return true;
	}
	if (!PivotAnalysisPersistentObject.class.isAssignableFrom(analysisPersistentObject.getClass())) {
	    return false;
	}
	final PivotAnalysisPersistentObject pivotPersistentObject = (PivotAnalysisPersistentObject) analysisPersistentObject;
	if (!getSelectedDistributionProperties().equals(pivotPersistentObject.getSelectedDistributionProperties())) {
	    return false;
	}
	if (!getSelectedAggregationPropertiesWithWidth().equals(pivotPersistentObject.getSelectedAggregationPropertiesWithWidth())) {
	    return false;
	}
	if (!getAvailableAggregationProperties().equals(pivotPersistentObject.getAvailableAggregationProperties())) {
	    return false;
	}
	if (!getAvailableDistributionProperties().equals(pivotPersistentObject.getAvailableDistributionProperties())) {
	    return false;
	}
	if (!getSortedAggregations().equals(pivotPersistentObject.getSortedAggregations())) {
	    return false;
	}
	if (!getSortableAggregations().equals(pivotPersistentObject.getSortableAggregations())) {
	    return false;
	}
	if (!getSelectedDistributionProperties().equals(pivotPersistentObject.getSelectedDistributionProperties())) {
	    return false;
	}
	if (getDistributionColumnWidth() != pivotPersistentObject.getDistributionColumnWidth()) {
	    return false;
	}
	return true;
    }

    public List<IDistributedProperty> getAvailableDistributionProperties() {
	return Collections.unmodifiableList(availableDistributionProperties);
    }

    public List<IAggregatedProperty> getAvailableAggregationProperties() {
	return Collections.unmodifiableList(availableAggregationProperties);
    }

    public List<IDistributedProperty> getSelectedDistributionProperties() {
	return Collections.unmodifiableList(selectedDistributionProperties);
    }

    public List<Pair<IAggregatedProperty, Integer>> getSelectedAggregationPropertiesWithWidth() {
	return Collections.unmodifiableList(selectedAggregationProperties);
    }

    public List<IAggregatedProperty> getSelectedAggregationProperties() {
	final List<IAggregatedProperty> aggregationProperties = new ArrayList<IAggregatedProperty>();
	for (final Pair<IAggregatedProperty, Integer> columnPair : selectedAggregationProperties) {
	    aggregationProperties.add(columnPair.getKey());
	}
	return Collections.unmodifiableList(aggregationProperties);
    }

    public List<SortObject<IAggregatedProperty>> getSortedAggregations() {
	return Collections.unmodifiableList(sortedAggregations);
    }

    public Set<IAggregatedProperty> getSortableAggregations() {
	return Collections.unmodifiableSet(sortableAggregations);
    }

    public int getDistributionColumnWidth() {
	return distributionColumnWidth;
    }

    @Override
    public IAnalysisReportType getType() {
	return AnalysisReportType.PIVOT;
    }

}
