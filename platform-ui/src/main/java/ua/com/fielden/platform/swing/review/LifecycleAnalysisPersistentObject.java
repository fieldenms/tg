package ua.com.fielden.platform.swing.review;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ua.com.fielden.platform.reportquery.IDistributedProperty;
import ua.com.fielden.platform.swing.analysis.AbstractAnalysisPersistentObject;
import ua.com.fielden.platform.swing.analysis.AnalysisReportType;
import ua.com.fielden.platform.swing.analysis.IAnalysisReportPersistentObject;
import ua.com.fielden.platform.swing.analysis.IAnalysisReportType;
import ua.com.fielden.platform.types.ICategory;

/**
 * Object that stores all information about lifecycle analysis reports (charts) and can be used to save it in to the file or restore from file.
 * 
 * @author Jhou
 * 
 */
public class LifecycleAnalysisPersistentObject extends AbstractAnalysisPersistentObject {
    private final IDistributedProperty distributionProperty;
    private final IDistributedProperty lifecycleProperty;
    /**
     * Ordering by category for concrete lifecycle property.
     */
    private final ua.com.fielden.platform.types.Ordering<ICategory, IDistributedProperty> lifecycleOrdering;
    private final Set<ICategory> lifecycleCategories = new HashSet<ICategory>();

    private final Date from, to;
    private Boolean total;

    protected LifecycleAnalysisPersistentObject() {
	this.lifecycleProperty = null;
	this.distributionProperty = null;
	this.from = null;
	this.to = null;
	this.total = null;

	this.lifecycleOrdering = null;
    }

    /**
     * Creates new {@link LifecycleAnalysisPersistentObject} with name, distribution property and aggregations.
     * 
     * @param distributionTreePath
     * @param aggregationProperties
     */
    public LifecycleAnalysisPersistentObject(final IDistributedProperty lifecycleProperty, final IDistributedProperty distributionProperty, final Date from, final Date to, final ua.com.fielden.platform.types.Ordering<ICategory, IDistributedProperty> lifecycleOrdering, final List<ICategory> lifecycleCategories, final Boolean total, final boolean visible) {
	super(visible);
	this.lifecycleProperty = lifecycleProperty;
	this.distributionProperty = distributionProperty;
	this.from = from;
	this.to = to;
	this.total = total;

	this.lifecycleOrdering = lifecycleOrdering;
	this.lifecycleCategories.addAll(lifecycleCategories);
    }

    /**
     * Returns true if this {@link LifecycleAnalysisPersistentObject} is the same as the given one, otherwise it returns false.
     * 
     * @param analysisPersistentObject
     * @return
     */
    public boolean isIdentical(final IAnalysisReportPersistentObject analysisReportPersistentObject) {
	if (this == analysisReportPersistentObject) {
	    return true;
	}
	if (!LifecycleAnalysisPersistentObject.class.isAssignableFrom(analysisReportPersistentObject.getClass())) {
	    return false;
	}
	final LifecycleAnalysisPersistentObject analysisPersistentObject = (LifecycleAnalysisPersistentObject) analysisReportPersistentObject;
	if (getLifecycleProperty() == null && getLifecycleProperty() != analysisPersistentObject.getLifecycleProperty() || getLifecycleProperty() != null
		&& !getLifecycleProperty().equals(analysisPersistentObject.getLifecycleProperty())) {
	    return false;
	}
	if (getDistributionProperty() == null && getDistributionProperty() != analysisPersistentObject.getDistributionProperty() || getDistributionProperty() != null
		&& !getDistributionProperty().equals(analysisPersistentObject.getDistributionProperty())) {
	    return false;
	}
	if (getFrom() == null && getFrom() != analysisPersistentObject.getFrom() || getFrom() != null && !getFrom().equals(analysisPersistentObject.getFrom())) {
	    return false;
	}
	if (getTo() == null && getTo() != analysisPersistentObject.getTo() || getTo() != null && !getTo().equals(analysisPersistentObject.getTo())) {
	    return false;
	}
	if (getTotal() == null && getTotal() != analysisPersistentObject.getTotal() || getTotal() != null && !getTotal().equals(analysisPersistentObject.getTotal())) {
	    return false;
	}
	if (lifecycleCategories == null && lifecycleCategories != analysisPersistentObject.lifecycleCategories || lifecycleCategories != null
		&& !lifecycleCategories.equals(analysisPersistentObject.lifecycleCategories)) {
	    return false;
	}
	if (getLifecycleOrdering() == null && getLifecycleOrdering() != analysisPersistentObject.getLifecycleOrdering() || getLifecycleOrdering() != null
		&& !getLifecycleOrdering().equals(analysisPersistentObject.getLifecycleOrdering())) {
	    return false;
	}
	return true;
    }

    /**
     * The property which lifecycle should be analyzed.
     * 
     * @return
     */
    public IDistributedProperty getLifecycleProperty() {
	return lifecycleProperty;
    }

    /**
     * Left margin for date interval.
     * 
     * @return
     */
    public Date getFrom() {
	return from;
    }

    /**
     * Right margin for date interval.
     * 
     * @return
     */
    public Date getTo() {
	return to;
    }

    public ua.com.fielden.platform.types.Ordering<ICategory, IDistributedProperty> getLifecycleOrdering() {
	return lifecycleOrdering;
    }

    public List<ICategory> getLifecycleCategories() {
	return new ArrayList<ICategory>(lifecycleCategories);
    }

    public IDistributedProperty getDistributionProperty() {
	return distributionProperty;
    }

    @Override
    public IAnalysisReportType getType() {
	return AnalysisReportType.LIFECYCLE;
    }

    public Boolean getTotal() {
	if (total == null) {
	    total = true;
	}
	return total;
    }

}
