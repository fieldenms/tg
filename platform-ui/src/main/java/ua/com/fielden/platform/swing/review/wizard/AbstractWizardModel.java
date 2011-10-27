package ua.com.fielden.platform.swing.review.wizard;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.actionpanelmodel.ActionPanelBuilder;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.swing.analysis.IAnalysisReportPersistentObject;
import ua.com.fielden.platform.swing.egi.AbstractPropertyColumnMapping;
import ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder;
import ua.com.fielden.platform.swing.review.DynamicCriteriaModelBuilder;
import ua.com.fielden.platform.swing.review.DynamicCriteriaPersistentObjectUi;
import ua.com.fielden.platform.swing.review.DynamicEntityQueryCriteria;
import ua.com.fielden.platform.swing.review.DynamicEntityReview;
import ua.com.fielden.platform.swing.review.DynamicEntityReviewModel;
import ua.com.fielden.platform.swing.review.DynamicProperty;
import ua.com.fielden.platform.swing.review.IPropertyListChangedListener;
import ua.com.fielden.platform.swing.review.LocatorPersistentObject;
import ua.com.fielden.platform.swing.review.PropertyColumnMappingsPersistentObject;
import ua.com.fielden.platform.swing.review.PropertyPersistentObject;
import ua.com.fielden.platform.swing.review.factory.IEntityReviewFactory;
import ua.com.fielden.platform.swing.review.optionbuilder.ActionChangerBuilder;
import ua.com.fielden.platform.treemodel.EntityTreeModel;
import ua.com.fielden.platform.treemodel.IPropertyFilter;
import ua.com.fielden.platform.utils.Pair;

/**
 * Model for {@link AbstractWizard}.
 *
 * @author TG Team
 *
 */
public abstract class AbstractWizardModel<T extends AbstractEntity, DAO extends IEntityDao<T>, R extends AbstractEntity> {
    private final DynamicEntityQueryCriteria<T, DAO> dynamicCriteria;
    // The class instance and paths needed for creating criteria
    private final Class<R> resultantEntityClass;

    private final Map<String, IAnalysisReportPersistentObject> analysis = new HashMap<String, IAnalysisReportPersistentObject>();
    private LocatorPersistentObject locatorPersistentObject = null;
    private PropertyColumnMappingsPersistentObject orderedMappingsPObj;
    private Map<String, PropertyPersistentObject> persistedCriteria = new HashMap<String, PropertyPersistentObject>();
    private boolean autoRun = false;

    private final DynamicCriteriaModelBuilder<T, DAO, R> modelBuilder;

    private final SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1, 4, 1);

    /**
     * Indicates whether suggestion was just provided due to addition/removal of dynamic properties and not because of user action.
     */
    private boolean suggestionWasProvided = false;

    /**
     * Indicates whether columns count suggestion should be provided upon addition/removal of dynamic properties.
     */
    private boolean provideSuggestions;
    private final EntityTreeModel treeModel;
    private final DynamicCriteriaPersistentObjectUi persistentObject;

    public DynamicCriteriaPersistentObjectUi getPersistentObject() {
	return persistentObject;
    }

    public EntityTreeModel getTreeModel() {
	return treeModel;
    }

    /**
     * Creates instance of {@link AbstractWizardModel} and sets {@link #provideSuggestions} flag to false and sets columns count to the specified value.
     *
     * @param dynamicCriteria
     * @param columnsCount
     */
    public AbstractWizardModel(//
	    final DynamicEntityQueryCriteria<T, DAO> dynamicCriteria,//
	    final Class<R> resultantEntityClass,//
	    final DynamicCriteriaPersistentObjectUi persistentObject,//
	    final DynamicCriteriaModelBuilder<T, DAO, R> modelBuilder) {
	this.resultantEntityClass = resultantEntityClass;
	this.dynamicCriteria = dynamicCriteria;
	this.persistentObject = persistentObject;
	this.provideSuggestions = persistentObject != null ? persistentObject.isProvideSuggestions() : true;
	this.modelBuilder = modelBuilder;
	dynamicCriteria.addPropertyListChangeListener(new PropertyListChangedHandler());

	if (persistentObject != null) {
	    persistedCriteria.putAll(persistentObject.getCriteriaMappings());
	    removeAllUnusedCriteriaPrpoerties();
	    orderedMappingsPObj = persistentObject.getPropertyColumnMappings();
	    analysis.putAll(persistentObject.getAnalysis());
	    locatorPersistentObject = new LocatorPersistentObject(persistentObject.getLocatorPersistentObject());
	    autoRun = persistentObject.isAutoRun();
	} else {
	    locatorPersistentObject = new LocatorPersistentObject();
	    orderedMappingsPObj = new PropertyColumnMappingsPersistentObject(new ArrayList<AbstractPropertyColumnMapping>(), null, null);
	}
	locatorPersistentObject.setBaseConfigurationManager(modelBuilder.getKey(), modelBuilder.getConfigurationController());

	if (provideSuggestions) {
	    spinnerModel.addChangeListener(createUserChangeListener());
	    setSpinnerValueManually(suggestValueForSpinner());
	} else {
	    setSpinnerValueManually(persistentObject != null ? persistentObject.getColumnsCount() : 1);
	}
	spinnerModel.addChangeListener(createReshuffleListener());
	this.treeModel = createTreeModel();
	addCritOnlyProperties();
    }

    private void addCritOnlyProperties() {
	final List<Field> critOnlyFields = Finder.findProperties(getEntityClass(), CritOnly.class);
	for (final Field field : critOnlyFields) {
	    addCriteriaProperty(field.getName());
	}
    }

    private void removeAllUnusedCriteriaPrpoerties() {
	final Set<String> actualCriteriaProperties = getDynamicCriteria().getKeySet();
	final Set<String> valuesToRemove = new HashSet<String>();
	for (final String persistedKey : getPersistedCriteria().keySet()) {
	    if (!actualCriteriaProperties.contains(persistedKey)) {
		valuesToRemove.add(persistedKey);
	    }
	}
	if (valuesToRemove.size() > 0) {
	    for (final String removeKey : valuesToRemove) {
		getPersistedCriteria().remove(removeKey);
	    }
	    for (final PropertyPersistentObject persistentObject : getPersistedCriteria().values()) {
		persistentObject.setPosition(null);
	    }
	}
    }

    private ChangeListener createReshuffleListener() {
	return new ChangeListener() {
	    @Override
	    public void stateChanged(final ChangeEvent e) {
		forceReshuffle();
	    }
	};
    }

    public DynamicCriteriaModelBuilder<T, DAO, R> getModelBuilder() {
	return modelBuilder;
    }

    private void setSpinnerValueManually(final Integer value) {
	suggestionWasProvided = true;
	// following call may invoke ChangeListener.stateChanged method, so that we indicate that that was suggestive set.
	spinnerModel.setValue(value);
	// after, whether stateChanged method was called or not, we should set suggestive set flag to false.
	suggestionWasProvided = false;
    }

    public void setOrderedMappingsPObj(final PropertyColumnMappingsPersistentObject orderedMappingsPObj) {
	this.orderedMappingsPObj = orderedMappingsPObj;
    }

    public int getColumnsCount() {
	return (Integer) spinnerModel.getValue();
    }

    public LocatorPersistentObject getLocatorPersistentObject() {
	return new LocatorPersistentObject(locatorPersistentObject);
    }

    public void setLocatorPersistentObject(final LocatorPersistentObject locatorPersistentObject) {
	this.locatorPersistentObject = new LocatorPersistentObject(locatorPersistentObject);
    }

    /**
     * Sets {@link #provideSuggestions} flag to false if it was user-initiated change.
     *
     * @return
     */
    private ChangeListener createUserChangeListener() {
	return new ChangeListener() {
	    @Override
	    public void stateChanged(final ChangeEvent e) {
		if (suggestionWasProvided) {
		    suggestionWasProvided = false;
		} else {
		    provideSuggestions = false;
		    spinnerModel.removeChangeListener(this);
		}
	    }
	};
    }

    /**
     * Forces criteria re-shuffle.
     */
    private void forceReshuffle() {
	for (final PropertyPersistentObject persistedProperty : getPersistedCriteria().values()) {
	    persistedProperty.setPosition(null);
	}
    }

    public Class<T> getEntityClass() {
	return getDynamicCriteria().getEntityClass();
    }

    public Class<R> getResultantEntityClass() {
	return resultantEntityClass != null ? resultantEntityClass : (Class<R>) getEntityClass();
    }

    public DynamicEntityQueryCriteria<T, DAO> getDynamicCriteria() {
	return dynamicCriteria;
    }

    public SpinnerModel getSpinnerModel() {
	return spinnerModel;
    }

    public PropertyColumnMappingsPersistentObject getOrderedMappingsPObj() {
	return orderedMappingsPObj;
    }

    protected abstract EntityTreeModel createTreeModel();

    //    {
    //	return new CriteriaTreeModel(getEntityClass(), propertyFilter);
    //    }

    public DynamicEntityReview<T, DAO, R> getEntityReview(final IEntityReviewFactory<T, DAO, R> entityReviewModelFactory, final ActionChangerBuilder actionChangerBuilder, final ActionPanelBuilder panelBuilder, final boolean isPrinciple) {
	final List oldPropertyMappings = new ArrayList(orderedMappingsPObj.getPropertyColumnMappings());
	final DynamicEntityReviewModel<T, DAO, R> dynamicEntityReviewModel = entityReviewModelFactory.createModel(dynamicCriteria, createPropertyTableModelBuilder(), actionChangerBuilder, panelBuilder, (Integer) spinnerModel.getValue(), persistedCriteria, getLocatorPersistentObject());
	if (modelBuilder.getOrderSetter() != null) {
	    dynamicEntityReviewModel.setOrdering(modelBuilder.getOrderSetter());
	}
	for (final AbstractPropertyColumnMapping<T> columnProperty : dynamicEntityReviewModel.getTableModel().getPropertyColumnMappings()) {
	    if (Reflector.notSortable(columnProperty.getColumnClass())) {
		dynamicEntityReviewModel.setSortable(columnProperty.getPropertyName(), false);
	    }
	}
	dynamicEntityReviewModel.initSorterWith(oldPropertyMappings, orderedMappingsPObj.getSortKeys(), orderedMappingsPObj.getIsSortable());
	final DynamicEntityReview<T, DAO, R> dynamicEntityReview = entityReviewModelFactory.createView(dynamicEntityReviewModel, isAutoRun(), modelBuilder, isPrinciple);
	return dynamicEntityReview;
    }

    public final DynamicEntityReview<T, DAO, R> getEntityReview(final ActionChangerBuilder actionChangerBuilder, final ActionPanelBuilder panelBuilder, final boolean showRecords, final boolean isPrinciple) {
	return getEntityReview(modelBuilder.getEntityReviewFactory(), actionChangerBuilder, panelBuilder, isPrinciple);
    }

    public void addTableHeader(final String header) {
	dynamicCriteria.addFetchProperty(header);
    }

    public void removeTableHeader(final String header) {
	dynamicCriteria.removeFetchProperty(header);
    }

    public void addExcludeProperty(final String excludeProperty) {
	dynamicCriteria.addExcludeProperty(excludeProperty);
    }

    public void removeExcludeProperty(final String excludeProperty) {
	dynamicCriteria.removeExcludeProperty(excludeProperty);
    }

    public void addCriteriaProperty(final String criteriaProperty) {
	dynamicCriteria.addProperty(criteriaProperty);

	final Pair<String, String> propertyNames = getDynamicCriteria().getPropertyNames(criteriaProperty);
	if (!persistedCriteria.containsKey(propertyNames.getKey())) {
	    persistedCriteria.put(propertyNames.getKey(), new PropertyPersistentObject(propertyNames.getKey()));
	}
	if (propertyNames.getValue() != null && !persistedCriteria.containsKey(propertyNames.getValue())) {
	    persistedCriteria.put(propertyNames.getValue(), new PropertyPersistentObject(propertyNames.getValue()));
	}
    }

    public void removeCriteriaProperty(final String property) {
	final Pair<String, String> propertyNames = getDynamicCriteria().getPropertyNames(property);
	persistedCriteria.remove(propertyNames.getKey());
	if (propertyNames.getValue() != null) {
	    persistedCriteria.remove(propertyNames.getValue());
	}
	dynamicCriteria.removeProperty(property);
    }

    public List<String> getSelectedCriteriaProperties() {
	return dynamicCriteria.getCriteriaProperties();
    }

    public List<String> getExcludeProperties() {
	return dynamicCriteria.getExcludeProperties();
    }

    public Map<String, PropertyPersistentObject> getPersistedCriteria() {
	return persistedCriteria;
    }

    public List<String> getSelectedTableHeaders() {
	return dynamicCriteria.getFetchProperties();
    }

    private int suggestValueForSpinner() {
	int twoColumnsEditors = 0, oneColumnEditors = 0;
	for (final String key : dynamicCriteria.getKeySet()) {
	    final DynamicProperty dynamicProperty = dynamicCriteria.getEditableProperty(key);
	    if (dynamicProperty.isBoolProperty() || dynamicProperty.isRangeProperty()) {
		twoColumnsEditors++;
	    } else {
		oneColumnEditors++;
	    }
	}
	return twoColumnsEditors > 0 ? 2 : 1;
    }

    @SuppressWarnings("rawtypes")
    private PropertyTableModelBuilder<T> createPropertyTableModelBuilder() {
	PropertyTableModelBuilder entityTableModelBuilder = new PropertyTableModelBuilder(getResultantEntityClass());
	final List<AbstractPropertyColumnMapping> orderedMappings = new ArrayList<AbstractPropertyColumnMapping>(orderedMappingsPObj.getPropertyColumnMappings());
	if (orderedMappings.isEmpty()) {
	    for (final String property : getSelectedTableHeaders()) {
		entityTableModelBuilder = entityTableModelBuilder.addReadonly(property, 80, getEntityClass()); // getDynamicCriteria().getRootAlias()+"."+
	    }
	} else {
	    final List<AbstractPropertyColumnMapping> mappingsToBeRemoved = determineMappingsToBeRemoved(getSelectedTableHeaders(), orderedMappings);
	    orderedMappings.removeAll(mappingsToBeRemoved);

	    entityTableModelBuilder.getPropertyColumnMappings().addAll(orderedMappings);

	    for (final String header : determineNewHeaders(getSelectedTableHeaders(), orderedMappings)) {
		entityTableModelBuilder.addReadonly(header, 80, getEntityClass());
	    }
	}
	// Clears titles/descs before building EGI model and use unified TG algorithm for titles/descs. Prebuild mappings should not be modified.
	entityTableModelBuilder.clearColumnTitlesAndTooltips(entityTableModelBuilder.getPropertyColumnMappings());
	return entityTableModelBuilder;
    }

    /**
     * Determines headers that were not persisted and should be added after persisted ones.
     *
     * @param allHeaders
     * @param existingMappings
     * @return
     */
    @SuppressWarnings("rawtypes")
    private List<String> determineNewHeaders(final List<String> allHeaders, final List<AbstractPropertyColumnMapping> existingMappings) {
	final List<String> newHeaders = new ArrayList<String>();
	for (final String header : allHeaders) {
	    boolean headerPresent = false;
	    for (final AbstractPropertyColumnMapping mapping : existingMappings) {
		if (mapping.getPropertyName().equals(header)) {
		    headerPresent = true;
		    break;
		}
	    }
	    if (!headerPresent) {
		newHeaders.add(header);
	    }
	}
	return newHeaders;
    }

    /**
     * Determines mappings that are not among list of all headers and thus should be removed.
     *
     * @param allHeaders
     * @param existingMappings
     * @return
     */
    @SuppressWarnings("rawtypes")
    private List<AbstractPropertyColumnMapping> determineMappingsToBeRemoved(final List<String> allHeaders, final List<AbstractPropertyColumnMapping> existingMappings) {
	final List<AbstractPropertyColumnMapping> mappingsToBeRemoved = new ArrayList<AbstractPropertyColumnMapping>();
	for (final AbstractPropertyColumnMapping mapping : existingMappings) {
	    boolean mappingPresent = false;
	    for (final String header : allHeaders) {
		if (mapping.getPropertyName().equals(header)) {
		    mappingPresent = true;
		    break;
		}
	    }
	    if (!mappingPresent) {
		mappingsToBeRemoved.add(mapping);
	    }
	}
	return mappingsToBeRemoved;
    }

    public DynamicCriteriaPersistentObjectUi createPersistentObject() {
	return new DynamicCriteriaPersistentObjectUi(getLocatorPersistentObject(), getSelectedTableHeaders(), getSelectedCriteriaProperties(), getExcludeProperties(), orderedMappingsPObj, persistedCriteria, getColumnsCount(), isProvideSuggestions(), analysis, isAutoRun(), false, false, true);
    }

    public boolean isProvideSuggestions() {
	return provideSuggestions;
    }

    public void setAnalysis(final Map<String, IAnalysisReportPersistentObject> analysis) {
	this.analysis.clear();
	this.analysis.putAll(analysis);
    }

    public Map<String, IAnalysisReportPersistentObject> getAnalysis() {
	return Collections.unmodifiableMap(analysis);
    }

    private class PropertyListChangedHandler implements IPropertyListChangedListener {

	@Override
	public void propertyAdded(final String key, final DynamicProperty dynamicProperty, final String propertyName) {
	    if (provideSuggestions) {
		setSpinnerValueManually(suggestValueForSpinner());
	    }
	    if (StringUtils.isEmpty(propertyName)) {
		// adding predef prop
		final PropertyPersistentObject persistedProperty = new PropertyPersistentObject(key);
		persistedProperty.setPropertyValue(dynamicProperty.getValue());
		persistedProperty.setNot(dynamicProperty.getNot());
		persistedProperty.setExclusive(dynamicProperty.getExclusive());
		persistedProperty.setDatePrefix(dynamicProperty.getDatePrefix());
		persistedProperty.setDateMnemonic(dynamicProperty.getDateMnemonic());
		persistedProperty.setAndBefore(dynamicProperty.getAndBefore());
		persistedProperty.setAll(dynamicProperty.getAll());
		persistedProperty.setOrNull(dynamicProperty.getOrNull());
		persistedCriteria.put(key, persistedProperty);
	    }
	}

	@Override
	public void propertyRemoved(final String key, final DynamicProperty proerty, final String propertyName) {
	    if (provideSuggestions) {
		setSpinnerValueManually(suggestValueForSpinner());
	    }
	}

    }

    public IPropertyFilter getPropertyFilter() {
	return getDynamicCriteria().getCriteriaFilter();
    }

    public boolean isAutoRun() {
	return autoRun;
    }

    public void setAutoRun(final boolean autoRun) {
	this.autoRun = autoRun;
    }

}
