package ua.com.fielden.platform.swing.review.report.analysis.lifecycle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import org.joda.time.DateTime;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.centre.analyses.ILifecycleDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.LifecycleDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.LifecycleDomainTreeRepresentation.LifecycleAddToCategoriesTickRepresentation;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.equery.lifecycle.LifecycleModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.components.bind.development.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.smart.datepicker.DatePickerLayer;
import ua.com.fielden.platform.swing.ei.development.MasterPropertyBinder;
import ua.com.fielden.platform.swing.ei.editors.development.IPropertyEditor;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReviewModel;
import ua.com.fielden.platform.types.ICategory;
import ua.com.fielden.platform.utils.Pair;

public class LifecycleAnalysisModel<T extends AbstractEntity<?>> extends AbstractAnalysisReviewModel<T, ICentreDomainTreeManagerAndEnhancer, ILifecycleDomainTreeManager, Void>{

    private final LifecycleChartFactory<T> chartFactory;

    private final LifecyclePropertiesUpdater propertyUpdater;

    private final EventListenerList listeners = new EventListenerList();

    private final Map<String, IPropertyEditor> propertyEditors;
    /**
     * The model to store the query result.
     */
    private LifecycleModel<T> lifecycleModel;

    public LifecycleAnalysisModel(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria, final ILifecycleDomainTreeManager adtme) {
	super(criteria, adtme);
	this.chartFactory = createChartFactory();
	this.lifecycleModel = null;

	this.propertyUpdater = getCriteria().getEntityFactory().newByKey(LifecyclePropertiesUpdater.class, "NOT REQUIRED");
	this.propertyUpdater.setLdtm(adtme);
	this.propertyEditors = MasterPropertyBinder.createPropertyBinderWithoutLocatorSupport(null, "key").bind(propertyUpdater);
    }

    public void addLifecycleModelUpdatedListener(final ILifecycleModelUpdated l){
	listeners.add(ILifecycleModelUpdated.class, l);
    }

    public void removeLifecycleUpdatedListener(final ILifecycleModelUpdated l){
	listeners.remove(ILifecycleModelUpdated.class, l);
    }

    @Override
    protected Void executeAnalysisQuery() {
	final EntityResultQueryModel<T> notOrderedQuery = DynamicQueryBuilder.createQuery(getCriteria().getManagedType(), getCriteria().createQueryProperties()).model();
	final List<String> fetchProperties = new ArrayList<>();
	for(final String distrProp : adtme().getFirstTick().checkedProperties(getCriteria().getEntityClass())){
	    if(!LifecycleAddToCategoriesTickRepresentation.isDatePeriodProperty(getCriteria().getManagedType(), distrProp)){
		fetchProperties.add(distrProp);
	    }
	}
	lifecycleModel = getCriteria().getLifecycleInformation(notOrderedQuery, fetchProperties, adtme().getLifecycleProperty().getValue(), new DateTime(adtme().getFrom()), new DateTime(adtme().getTo()));
	SwingUtilities.invokeLater(new Runnable() {

	    @Override
	    public void run() {
		fireLifecycleModelUpdated(new LifecycleModelUpdateEvent<>(LifecycleAnalysisModel.this, lifecycleModel));
	    }
	});
	return null;
    }

    @Override
    protected void exportData(final String fileName) throws IOException {
	throw new UnsupportedOperationException("The data exporting for lifecycle analysis is not supported!");
    }

    @Override
    protected Result canLoadData() {
	final Result res = getCriteria().isValid();
	if(!res.isSuccessful()){
	    return res;
	}
	final Result lifecycleResult = propertyUpdater.isValid();
	if(!lifecycleResult.isSuccessful()){
	    return lifecycleResult;
	}
	if(getLifecycleProperty() == null){
	    return new Result(new IllegalStateException("<html>Please specify the <b>lifecycle property</b>.</html>"));
	}
	return Result.successful(this);
    }

    @Override
    protected String[] getExportFileExtensions() {
	throw new UnsupportedOperationException("The data exporting for lifecycle analysis is not supported!");
    }

    @Override
    protected String getDefaultExportFileExtension() {
	throw new UnsupportedOperationException("The data exporting for lifecycle analysis is not supported!");
    }

    protected void fireLifecycleModelUpdated(final LifecycleModelUpdateEvent<T> event){
	for(final ILifecycleModelUpdated listener : listeners.getListeners(ILifecycleModelUpdated.class)){
	    listener.lifecycleModelUpdated(event);
	}
    }

    /**
     * Creates the chart factory for this Lifecycle analysis model. Override this to provide custom chart factory.
     *
     * @return
     */
    protected LifecycleChartFactory<T> createChartFactory(){
	return new LifecycleChartFactory<>(this);
    }

    /**
     * Returns the associated {@link LifecycleChartFactory} instance, that was created with {@link #createChartFactory()}.
     *
     * @return
     */
    public LifecycleChartFactory<T> getChartFactory() {
	return chartFactory;
    }

    /**
     * Returns the binded {@link DatePickerLayer} for the "from" property editor.
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public BoundedValidationLayer<DatePickerLayer> getFromeEditor(){
	return (BoundedValidationLayer<DatePickerLayer>)propertyEditors.get("from").getEditor();
    }

    /**
     * Returns the binded {@link DatePickerLayer} for the "to" property editor.
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public BoundedValidationLayer<DatePickerLayer> getToEditor(){
	return (BoundedValidationLayer<DatePickerLayer>)propertyEditors.get("to").getEditor();
    }

    //////////////////////// Recently added stubs ////////////////////////

    /**
     * TODO please note that ordering will be performed only on one property.
     * It should be extended to many properties.
     *
     * @return
     */
    public Pair<ICategory, Ordering> getOrdering() {
	final List<Pair<String, Ordering>> orderings = adtme().getSecondTick().orderedProperties(getCriteria().getEntityClass());
	if(orderings.isEmpty()){
	    return null;
	}
	final Pair<String, Ordering> firstOrdering = orderings.get(0);
	return new Pair<ICategory, Ordering>(LifecycleDomainTreeManager.getCategory(getCriteria().getManagedType(), firstOrdering.getKey(), adtme().getSecondTick().allCategories(getCriteria().getEntityClass())), firstOrdering.getValue());
    }

    public boolean getTotal() {
	return adtme().isTotal();
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

    public String getDistributionProperty() {
	// TODO consider empty
	return adtme().getFirstTick().usedProperties(getCriteria().getEntityClass()).get(0);
    }

    /**
     * Returns current categories (used properties for second tick).
     *
     * @param property
     * @return
     */
    public List<? extends ICategory> getCurrentCategories() {
	return adtme().getSecondTick().currentCategories(getCriteria().getEntityClass());
    }

    public ICategory findCategoryByName(final String info) {
	for (final ICategory c : getCurrentCategories()) {
	    if (c.getName().equals(info)) {
		return c;
	    }
	}
	return null;
    }

    public String getLifecycleProperty() {
	return adtme().getLifecycleProperty().getValue();
    }

    public List<? extends ICategory> allCategories() {
	return adtme().getSecondTick().allCategories(getCriteria().getEntityClass());
    }
}
