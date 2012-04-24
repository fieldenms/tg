package ua.com.fielden.platform.swing.ei.editors.development;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.ILocatorManager;
import ua.com.fielden.platform.domaintree.ILocatorManager.Phase;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.swing.components.bind.development.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory;
import ua.com.fielden.platform.swing.components.smart.autocompleter.development.AutocompleterTextFieldLayer;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.swing.review.development.EnhancedLocatorEntityQueryCriteria;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.centre.configuration.LocatorConfigurationModel;
import ua.com.fielden.platform.utils.Pair;

public class EntityPropertyEditorWithLocator extends AbstractEntityPropertyEditor {

    private final BoundedValidationLayer<AutocompleterTextFieldLayer> editor;

    /**
     * Creates standard {@link EntityPropertyEditorWithLocator} editor with entity locator for entity centre.
     *
     * @return
     */
    public static EntityPropertyEditorWithLocator createEntityPropertyEditorWithLocatorForCentre(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, ?, ?> criteria, final String propertyName, final ICriteriaGenerator criteriaGenerator){
	final String criteriaPropertyName = CriteriaReflector.getCriteriaProperty(criteria.getClass(), propertyName);
	final IValueMatcher<?> valueMatcher = criteria.getValueMatcher(propertyName);
	final MetaProperty metaProp = criteria.getProperty(propertyName);

	return createEntityPropertyEditorWithLocator(//
		criteria,//
		propertyName,//
		criteria.getEntityClass(),//
		criteriaPropertyName,//
		criteria.getCentreDomainTreeMangerAndEnhancer().getFirstTick(),//
		criteriaGenerator,//
		valueMatcher,//
		LabelAndTooltipExtractor.createCaption(metaProp.getTitle()),//
		LabelAndTooltipExtractor.createTooltip(metaProp.getDesc()));
    }

    public static EntityPropertyEditorWithLocator createEntityPropertyEditorWithLocatorForMaster(final AbstractEntity<?> entity, final String propertyName, final ILocatorManager locatorManager, final ICriteriaGenerator criteriaGenerator, final IValueMatcher<?> valueMatcher){
	//createEditor(entity, propertyName, property.getType(), "", property.getDesc(), entity.getEntityFactory(), entityMasterFactory, vmf, daoFactory, locatorController, locatorRetriever);
	final MetaProperty metaProp = entity.getProperty(propertyName);
	final String toolTip = metaProp.getDesc();

	return createEntityPropertyEditorWithLocator(//
		entity,//
		propertyName,//
		entity.getType(),//
		propertyName,//
		locatorManager, //
		criteriaGenerator,//
		valueMatcher,//
		"",//
		toolTip);
    }

    private static EntityPropertyEditorWithLocator createEntityPropertyEditorWithLocator(final AbstractEntity<?> entity, //
	    final String propertyName, //
	    final Class<?> rootType, //
	    final String locatorName, //
	    final ILocatorManager locatorManager, //
	    final ICriteriaGenerator criteriaGenerator, //
	    final IValueMatcher<?> valueMatcher, //
	    final String caption, //
	    final String toolTip){
	final MetaProperty metaProp = entity.getProperty(propertyName);
	final IsProperty propertyAnnotation = AnnotationReflector.getPropertyAnnotation(IsProperty.class, entity.getType(), propertyName);
	final EntityType entityTypeAnnotation = AnnotationReflector.getPropertyAnnotation(EntityType.class, entity.getType(), propertyName);
	final boolean isSingle = isSingle(entity, propertyName);
	final boolean stringBinding = isStringBinded(entity, propertyName);
	final Class<?> elementType = isSingle ? metaProp.getType() : (stringBinding ? entityTypeAnnotation.value() : propertyAnnotation.value());
	if(!AbstractEntity.class.isAssignableFrom(elementType)){
	    throw new IllegalArgumentException("The property: " + propertyName + " of " + entity.getType().getSimpleName() + " type, can not be bind to the autocompleter!");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	final LocatorConfigurationModel locatorConfigurationModel = new LocatorConfigurationModel(
		elementType,//
		rootType,//
		locatorName,//
		locatorManager,//
		entity.getEntityFactory(),//
		criteriaGenerator);

	return new EntityPropertyEditorWithLocator(entity, propertyName, locatorConfigurationModel, elementType, valueMatcher, caption, toolTip);
    }

    private static boolean isSingle(final AbstractEntity<?> entity, final String propertyName){
	final MetaProperty metaProp = entity.getProperty(propertyName);
	return !metaProp.isCollectional();
    }

    private static boolean isStringBinded(final AbstractEntity<?> entity, final String propertyName){
	final IsProperty propertyAnnotation = AnnotationReflector.getPropertyAnnotation(IsProperty.class, entity.getType(), propertyName);
	final boolean isSingle = isSingle(entity, propertyName);
	return isSingle ? false : String.class.isAssignableFrom(propertyAnnotation.value());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public EntityPropertyEditorWithLocator(final AbstractEntity<?> entity, final String propertyName, final LocatorConfigurationModel locatorConfigurationModel, final Class<?> elementType, final IValueMatcher<?> valueMatcher, final String caption, final String toolTip) {
	super(entity, propertyName, new EntityLocatorValueMatcher(valueMatcher, locatorConfigurationModel.getLocatorManager(), locatorConfigurationModel.getCriteriaGenerator(), locatorConfigurationModel.getEntityType(), locatorConfigurationModel.getRootType(), locatorConfigurationModel.getName()));
	getValueMatcher().setBindedEntity(entity);
	editor = createEditorWithLocator(entity, propertyName, locatorConfigurationModel, elementType,//
		caption, toolTip, isSingle(entity, propertyName), isStringBinded(entity, propertyName));
    }

    @Override
    public BoundedValidationLayer<AutocompleterTextFieldLayer> getEditor() {
	return editor;
    }

    @Override
    public EntityLocatorValueMatcher getValueMatcher() {
	return (EntityLocatorValueMatcher)super.getValueMatcher();
    }

    @Override
    public void bind(final AbstractEntity<?> entity) {
	super.bind(entity);
	getValueMatcher().setBindedEntity(entity);
    }

    public void highlightFirstHintValue(final boolean highlight) {
	getEditor().getView().highlightFirstHintValue(highlight);
    }

    public void highlightSecondHintValue(final boolean highlight) {
	getEditor().getView().highlightSecondHintValue(highlight);
    }

    private BoundedValidationLayer<AutocompleterTextFieldLayer> createEditorWithLocator(//
	    final AbstractEntity bindingEntity,//
	    final String bindingPropertyName,//
	    final LocatorConfigurationModel locatorConfigurationModel, //
	    final Class entityType,//
	    final String caption,//
	    final String toolTip,//
	    final boolean isSingle,//
	    final boolean stringBinding//
	    ){
	if (!AbstractEntity.class.isAssignableFrom(entityType)) {
	    throw new RuntimeException("Could not determined an editor for property " + getPropertyName() + " of type " + entityType + ".");
	}
	return ComponentFactory.createOnFocusLostAutocompleterWithEntityLocator(bindingEntity, bindingPropertyName, locatorConfigurationModel, entityType, getValueMatcher(), "key", "desc", caption, isSingle ? null : ",", toolTip, stringBinding);
    }

    private static class EntityLocatorValueMatcher<T extends AbstractEntity<?>, R extends AbstractEntity<?>> implements IValueMatcher<T>{

	private final IValueMatcher<T> autocompleterValueMatcher;

	private final ILocatorManager locatorManager;

	private final ICriteriaGenerator criteriaGenerator;

	private final Class<T> entityType;

	private final Class<R> rootType;

	private final String propertyName;

	private AbstractEntity<?> bindedEntity;

	public EntityLocatorValueMatcher(//
		final IValueMatcher<T> autocompleterValueMatcher,//
		final ILocatorManager locatorManager,//
		final ICriteriaGenerator criteriaGenerator,//
		final Class<T> entityType,//
		final Class<R> rootType,//
		final String propertyName){
	    this.autocompleterValueMatcher = autocompleterValueMatcher;
	    this.locatorManager = locatorManager;
	    this.criteriaGenerator = criteriaGenerator;
	    this.entityType = entityType;
	    this.rootType = rootType;
	    this.propertyName = propertyName;
	}

	public void setBindedEntity(final AbstractEntity<?> bindedEntity) {
	    this.bindedEntity = bindedEntity;
	}

	public AbstractEntity<?> getBindedEntity() {
	    return bindedEntity;
	}

	@Override
	public <FT extends AbstractEntity<?>> fetch<FT> getFetchModel() {
	    return autocompleterValueMatcher.getFetchModel();
	}

	@Override
	public <FT extends AbstractEntity<?>> void setFetchModel(final fetch<FT> fetchModel) {
	    autocompleterValueMatcher.setFetchModel(fetchModel);
	}

	@Override
	public List<T> findMatches(final String value) {
	    return findMatches(value, null);
	}

	@Override
	public List<T> findMatchesWithModel(final String value) {
	    return findMatches(value, getFetchModel());
	}

	@SuppressWarnings("unchecked")
	private List<T> findMatches(final String value, final fetch<?> fetchModel) {
	    final ILocatorDomainTreeManagerAndEnhancer ldtme = ldtme();
	    if(ldtme.isUseForAutocompletion()){
		final MetaProperty searchProp = getBindedEntity().getProperty(propertyName);
		final List<Pair<String, Object>> dependentValues = new ArrayList<Pair<String,Object>>();
		if (searchProp != null) {
		    for (final String dependentProperty : searchProp.getDependentPropertyNames()) {
			if (Finder.isPropertyPresent(searchProp.getType(), dependentProperty)) {
			    dependentValues.add(new Pair<String, Object>(dependentProperty, getBindedEntity().get(dependentProperty)));
			}
		    }
		}
		final EnhancedLocatorEntityQueryCriteria<T, IEntityDao<T>> criteria = criteriaGenerator.generateLocatorQueryCriteria(entityType, ldtme);
		return criteria.runLocatorQuery(getPageSize(), value, fetchModel, dependentValues.toArray(new Pair[0]));
	    }else{
		if(fetchModel == null){
		    return autocompleterValueMatcher.findMatches(value);
		} else {
		    return autocompleterValueMatcher.findMatchesWithModel(value);
		}
	    }

	}

	private ILocatorDomainTreeManagerAndEnhancer ldtme(){
	    if(Phase.USAGE_PHASE != locatorManager.phaseAndTypeOfLocatorManager(rootType, propertyName).getKey()){
		throw new IllegalStateException("The locator must be in usage mode!");
	    }
	    locatorManager.refreshLocatorManager(rootType, propertyName);
	    final ILocatorDomainTreeManagerAndEnhancer ldtme = locatorManager.getLocatorManager(rootType, propertyName);
	    if(ldtme == null){
		throw new IllegalStateException("The locator manager must be initialised");
	    }
	    locatorManager.discardLocatorManager(rootType, propertyName);
	    return ldtme;
	}

	@Override
	public Integer getPageSize() {
	    return autocompleterValueMatcher.getPageSize();
	}
    }

}
