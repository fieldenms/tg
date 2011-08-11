package ua.com.fielden.platform.swing.analysis.lifecycle;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.lifecycle.EntityPropertyLifecycle;
import ua.com.fielden.platform.equery.lifecycle.LifecycleModel.GroupingPeriods;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.reportquery.DistributionProperty;
import ua.com.fielden.platform.reportquery.IDistributedProperty;
import ua.com.fielden.platform.swing.analysis.AbstractAnalysisReportModel;
import ua.com.fielden.platform.swing.analysis.DetailsFrame;
import ua.com.fielden.platform.swing.analysis.IAnalysisReportPersistentObject;
import ua.com.fielden.platform.swing.categorychart.AnalysisDoubleClickEvent;
import ua.com.fielden.platform.swing.review.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.EntityReviewModel;
import ua.com.fielden.platform.swing.review.LifecycleAnalysisPersistentObject;
import ua.com.fielden.platform.swing.review.analysis.LifecycleReportQueryCriteriaExtender;
import ua.com.fielden.platform.types.ICategory;
import ua.com.fielden.platform.types.Ordering;
import ua.com.fielden.platform.utils.Pair;

/**
 * The model for {@link LifecycleChartReview}.
 * 
 * @author Jhou
 * 
 */
public class LifecycleChartReviewModel<T extends AbstractEntity, DAO extends IEntityDao<T>> extends AbstractAnalysisReportModel<T, DAO> implements Cloneable {

    private final LifecycleReportQueryCriteriaExtender<T, DAO> lifecycleQueryExtender;
    /**
     * Lifecycle chart factory.
     */
    private final LifecycleChartFactory<T, DAO> lifecycleChartFactory = createLifecycleChartFactory();

    /**
     * Current lifecycle property.
     */
    private IDistributedProperty lifecycleProperty;

    /**
     * Current distribution property.
     */
    private IDistributedProperty distributionProperty;

    /**
     * Ordering by category for concrete lifecycle property.
     */
    private Ordering<ICategory, IDistributedProperty> ordering;
    /**
     * Holds all available properties that might be used for lifecycle report.
     */
    private final List<IDistributedProperty> lifecycleProperties;
    private final Map<IDistributedProperty, List<ICategory>> categoriesMap;
    private final Map<IDistributedProperty, List<IDistributedProperty>> distributionPropertiesMap;
    /**
     * Left/right margins for date intervals.
     */
    private Date from, to;

    /** Indicates whether total values should be used instead of average. Average is calculated based on group's entities count. */
    private Boolean total = true;

    protected LifecycleChartFactory<T, DAO> createLifecycleChartFactory() {
	return new LifecycleChartFactory<T, DAO>(this);
    }

    /**
     * Instantiates the {@link LifecycleChartReviewModel} instance.
     * 
     * @param criteria
     * 
     * @param afterRunActions
     */
    public LifecycleChartReviewModel(final EntityReviewModel<T, DAO, ? extends EntityQueryCriteria<T, DAO>> centerModel, final Map<String, Map<Object, DetailsFrame>> detailsFrame, final IAnalysisReportPersistentObject persistentObject, final String name, final String reportName) {
	super(centerModel, detailsFrame, name, reportName);

	this.lifecycleQueryExtender = new LifecycleReportQueryCriteriaExtender<T, DAO>();
	this.lifecycleQueryExtender.setBaseCriteria(centerModel.getCriteria());
	lifecycleProperties = initLifecycleProperties(centerModel.getCriteria().getEntityClass());
	categoriesMap = createCategoriesMap();
	distributionPropertiesMap = createDistributionPropertiesMap();
	if (persistentObject instanceof LifecycleAnalysisPersistentObject) {
	    final LifecycleAnalysisPersistentObject analysisItem = (LifecycleAnalysisPersistentObject) persistentObject;
	    setLifecycleProperty(analysisItem.getLifecycleProperty());
	    setDistributionProperty(analysisItem.getDistributionProperty());
	    setFrom(analysisItem.getFrom());
	    setTo(analysisItem.getTo());
	    setOrdering(analysisItem.getLifecycleOrdering());
	    setCategoriesFor(analysisItem.getLifecycleProperty(), analysisItem.getLifecycleCategories());
	    setTotal(analysisItem.getTotal());
	}
    }

    /**
     * Returns categories associated with current lifecycle property.
     * 
     * @param property
     * @return
     */
    public List<ICategory> getCurrentCategories() {
	final List<ICategory> list = categoriesMap.get(lifecycleProperty);
	return list == null ? null : Collections.unmodifiableList(list);
    }

    /**
     * Returns distribution properties associated with current lifecycle property.
     * 
     * @param property
     * @return
     */
    public List<IDistributedProperty> getCurrentDistributionProperties() {
	final List<IDistributedProperty> list = distributionPropertiesMap.get(lifecycleProperty);
	return list == null ? null : Collections.unmodifiableList(list);
    }

    /**
     * Returns string representations of categories associated with current lifecycle property.
     * 
     * @param property
     * @return
     */
    public List<String> getCurrentCategoriesStrings() {
	final List<String> l = new ArrayList<String>();
	for (final ICategory c : getCurrentCategories()) {
	    l.add(c.toString());
	}
	return l;
    }

    /**
     * Returns the categories associated with the specified lifecycle property.
     * 
     * @param property
     * @return
     */
    public List<ICategory> getCategoriesFor(final IDistributedProperty property) {
	return categoriesMap.get(property);
    }

    /**
     * Sets the categories for the specified lifecycle property.
     * 
     * @param property
     * @return
     */
    public void setCategoriesFor(final IDistributedProperty property, final List<ICategory> categories) {
	categoriesMap.put(property, categories);
    }

    private List<IDistributedProperty> initLifecycleProperties(final Class<T> clazz) {
	final List<IDistributedProperty> properties = new ArrayList<IDistributedProperty>();
	for (final Field field : Finder.findLifecycleProperties(clazz)) {
	    final String propertyName = field.getName();
	    properties.add(new DistributionProperty(TitlesDescsGetter.getTitleAndDesc(propertyName, clazz).getKey(), "<html>Explore lifecycle for <i>"
		    + TitlesDescsGetter.removeHtmlTag(TitlesDescsGetter.getFullTitleAndDesc(propertyName, clazz).getValue()) + "</i></html>", propertyName));
	}
	return properties;
    }

    private Map<IDistributedProperty, List<IDistributedProperty>> createDistributionPropertiesMap() {
	final Map<IDistributedProperty, List<IDistributedProperty>> distributionPropertiesMap = new HashMap<IDistributedProperty, List<IDistributedProperty>>();
	for (final IDistributedProperty lifecycleProperty : lifecycleProperties) {
	    distributionPropertiesMap.put(lifecycleProperty, distributionProperties(lifecycleProperty));
	}
	return distributionPropertiesMap;
    }

    private Map<IDistributedProperty, List<ICategory>> createCategoriesMap() {
	final Map<IDistributedProperty, List<ICategory>> categoriesMap = new HashMap<IDistributedProperty, List<ICategory>>();
	for (final IDistributedProperty lifecycleProperty : lifecycleProperties) {
	    categoriesMap.put(lifecycleProperty, Arrays.asList(mainCategories(lifecycleProperty)));
	}
	return categoriesMap;
    }

    private IDistributedProperty create(final Class<T> clazz, final String propertyName) {
	if ("".equals(propertyName)) { // high-level entity:
	    final Pair<String, String> td = TitlesDescsGetter.getEntityTitleAndDesc(clazz);
	    return new DistributionProperty(td.getKey(), "<html>Distribute by high-level <b><i>" + td.getValue() + "</i></b></html>", propertyName);
	} else {
	    return new DistributionProperty(TitlesDescsGetter.getTitleAndDesc(propertyName, clazz).getKey(), "<html>Distribute by <i>"
		    + TitlesDescsGetter.removeHtmlTag(TitlesDescsGetter.getFullTitleAndDesc(propertyName, clazz).getValue()) + "</i></html>", propertyName);
	}
    }

    public List<IDistributedProperty> distributionProperties(final IDistributedProperty lifecycleProperty) {
	if (lifecycleProperty == null) {
	    return Collections.emptyList();
	}
	final Class<T> entityClass = getCenterModel().getCriteria().getEntityClass();
	final List<String> distrPr = EntityPropertyLifecycle.getCategorizer(entityClass, lifecycleProperty.getActualProperty()).getDistributionProperties();

	final List<IDistributedProperty> result = new ArrayList<IDistributedProperty>();
	for (final String exp : distrPr) {
	    result.add(create(entityClass, exp));
	}
	result.addAll(defaultDistributionPeriodProperties(lifecycleProperty));
	return result;
    }

    public List<IDistributedProperty> defaultDistributionPeriodProperties(final IDistributedProperty lifecycleProperty) {
	if (lifecycleProperty == null) {
	    return Collections.emptyList();
	}
	final List<IDistributedProperty> result = new ArrayList<IDistributedProperty>();
	for (final GroupingPeriods period : GroupingPeriods.values()) {
	    result.add(new DistributionProperty(period.getTitle(), "<html>Distribute by <b><i>" + period.getDesc() + "</i></b></html>", period.getPropertyName()));
	}
	return result;
    }

    public ICategory[] mainCategories(final IDistributedProperty lifecycleProperty) {
	return lifecycleProperty == null ? new ICategory[0]
		: (ICategory[]) EntityPropertyLifecycle.getCategorizer(getCenterModel().getCriteria().getEntityClass(), lifecycleProperty.getActualProperty()).getMainCategories().toArray();
    }

    public ICategory[] allCategories(final IDistributedProperty lifecycleProperty) {
	return lifecycleProperty == null ? new ICategory[0]
		: (ICategory[]) EntityPropertyLifecycle.getCategorizer(getCenterModel().getCriteria().getEntityClass(), lifecycleProperty.getActualProperty()).getAllCategories().toArray();
    }

    void updateCriteria() throws IllegalStateException {

	final IDistributedProperty lifecycleProperty = getLifecycleProperty();
	if (lifecycleProperty == null) {
	    throw new IllegalStateException("<html>Please choose lifecycle property.</html>");
	}
	lifecycleQueryExtender.setLifecycleProperty(lifecycleProperty);
	lifecycleQueryExtender.setFrom(getFrom());
	lifecycleQueryExtender.setTo(getTo());
	if (lifecycleQueryExtender.getFrom() == null || lifecycleQueryExtender.getTo() == null) {
	    throw new IllegalStateException("<html>Please correct period <b>beginning</b> and <b>ending</b> to be non-empty.</html>");
	} else if (getFrom().after(getTo()) || getFrom().equals(getTo())) {
	    throw new IllegalStateException("<html>Please correct period to be with <b>beginning</b> less than <b>ending</b>.</html>");
	}

    }

    public IDistributedProperty getLifecycleProperty() {
	return lifecycleProperty;
    }

    public void setLifecycleProperty(final IDistributedProperty lifecycleProperty) {
	this.lifecycleProperty = lifecycleProperty;
    }

    public IDistributedProperty getDistributionProperty() {
	return distributionProperty;
    }

    public void setDistributionProperty(final IDistributedProperty distributionProperty) {
	this.distributionProperty = distributionProperty;
    }

    public Date getFrom() {
	return from;
    }

    public void setFrom(final Date from) {
	this.from = from;
    }

    public Date getTo() {
	return to;
    }

    public void setTo(final Date to) {
	this.to = to;
    }

    protected List<IDistributedProperty> getLifecycleProperties() {
	return lifecycleProperties;
    }

    /**
     * Returns the current {@link Ordering} instance.
     * 
     * @return
     */
    public Ordering<ICategory, IDistributedProperty> getOrdering() {
	return ordering;
    }

    /**
     * Set the currentOrdering for this {@link LifecycleChartReviewModel}.
     * 
     * @param ordering
     */
    public void setOrdering(final Ordering<ICategory, IDistributedProperty> ordering) {
	this.ordering = ordering;
    }

    @Override
    public Object runAnalysisQuery(final int pageSize) {
	return lifecycleQueryExtender.runExtendedQuery(pageSize);
    }

    public LifecycleChartFactory<T, DAO> getChartFactory(final boolean all, final int... indeces) {
	return lifecycleChartFactory;
    }

    @Override
    public void runDoubleClickAction(final AnalysisDoubleClickEvent analysisEvent) {
    }

    public ICategory findCategoryByName(final String info) {
	for (final ICategory c : getCurrentCategories()) {
	    if (c.getName().equals(info)) {
		return c;
	    }
	}
	return null;
    }

    /** Indicates whether total values should be used instead of average. Average is calculated based on group's entities count. */
    public Boolean getTotal() {
	return total;
    }

    /** Sets an indicator whether total values should be used instead of average. Average is calculated based on group's entities count. */
    public void setTotal(final Boolean total) {
	this.total = total;
    }
}
