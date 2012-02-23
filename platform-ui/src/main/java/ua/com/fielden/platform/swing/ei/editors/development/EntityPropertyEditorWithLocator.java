package ua.com.fielden.platform.swing.ei.editors.development;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector;
import ua.com.fielden.platform.domaintree.ILocatorManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.swing.components.bind.development.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory;
import ua.com.fielden.platform.swing.components.smart.autocompleter.development.AutocompleterTextFieldLayer;
import ua.com.fielden.platform.swing.ei.editors.LabelAndTooltipExtractor;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.utils.Pair;

public class EntityPropertyEditorWithLocator extends AbstractEntityPropertyEditor {

    private final BoundedValidationLayer<AutocompleterTextFieldLayer> editor;

    /**
     * Creates standard {@link EntityPropertyEditorWithLocator} editor with entity locator for entity centre.
     * 
     * @return
     */
    public static EntityPropertyEditorWithLocator createEntityPropertyEditorWithLocatorForCentre(final EntityQueryCriteria<ICentreDomainTreeManager, ?, ?> criteria, final String propertyName, final ICriteriaGenerator criteriaGenerator, final boolean isSingle){
	//TODO Refactor later after testing.
	final Pair<Class<?>, String> criteriaParameters = CriteriaReflector.getCriteriaProperty(criteria.getClass(), propertyName);
	final EntityType typeAnnotation = AnnotationReflector.getPropertyAnnotation(EntityType.class, criteria.getType(), propertyName);
	if(typeAnnotation == null){
	    throw new IllegalStateException("Please annotate field '" + propertyName + "' with annotation " + EntityType.class.getName());
	}
	final Class entityType = typeAnnotation.value();
	final Class rootType = criteriaParameters.getKey();
	final ILocatorManager locatorManager = criteria.getDomainTreeManger().getFirstTick();
	final IValueMatcher<?> valueMatcher = criteria.getValueMatcher(propertyName);
	final MetaProperty metaProp = criteria.getProperty(propertyName);
	final String caption = LabelAndTooltipExtractor.createCaption(metaProp.getTitle());
	final String toolTip = LabelAndTooltipExtractor.createTooltip(metaProp.getDesc());
	final boolean stringBinding = !PropertyDescriptor.class.equals(entityType);
	return new EntityPropertyEditorWithLocator(criteria, //
		propertyName, //
		entityType, //
		rootType, //
		locatorManager, //
		criteriaGenerator, //
		valueMatcher, //
		caption, //
		toolTip, //
		isSingle, //
		stringBinding);
    }

    public static EntityPropertyEditorWithLocator createEntityPropertyEditorWithLocatorForMaster(final AbstractEntity<?> entity, final String propertyName, final ILocatorManager locatorManager, final ICriteriaGenerator criteiraGenerator, final IValueMatcher<?> valueMatcher, final boolean isSingle){
	//createEditor(entity, propertyName, property.getType(), "", property.getDesc(), entity.getEntityFactory(), entityMasterFactory, vmf, daoFactory, locatorController, locatorRetriever);
	//TODO Refactor later after testing.
	final IsProperty propertyAnnotation = AnnotationReflector.getPropertyAnnotation(IsProperty.class, entity.getType(), propertyName);
	if(propertyAnnotation == null){
	    throw new IllegalStateException("Please annotate field '" + propertyName + "' with annotation " + EntityType.class.getName());
	}
	final Class entityType = propertyAnnotation.value();
	final Class rootType = entity.getType();
	final MetaProperty metaProp = entity.getProperty(propertyName);
	final String toolTip = metaProp.getDesc();
	return new EntityPropertyEditorWithLocator(entity, //
		propertyName, //
		entityType, //
		rootType, //
		locatorManager, //
		criteiraGenerator, //
		valueMatcher, //
		"", //
		toolTip, //
		isSingle, //
		false);
    }

    public EntityPropertyEditorWithLocator(final AbstractEntity<?> entity, final String propertyName, final Class<?> elementType, final Class<?> rootType, final ILocatorManager locatorManager, final ICriteriaGenerator criteiraGenerator, final IValueMatcher<?> valueMatcher, final String caption, final String toolTip, final boolean isSingle, final boolean stringBinding) {
	super(entity, propertyName, new EntityLocatorValueMatcher(valueMatcher, locatorManager, rootType, propertyName));
	getValueMatcher().setBindedEntity(entity);
	editor = createEditorWithLocator(entity, propertyName, elementType, rootType, caption, toolTip, locatorManager, criteiraGenerator, isSingle, stringBinding);
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
	    final Class entityType,//
	    final Class rootType,//
	    final String caption,//
	    final String toolTip,//
	    final ILocatorManager locatorManager,//
	    final ICriteriaGenerator criteriaGenerator,//
	    final boolean isSingle,//
	    final boolean stringBinding//
	    ){
	if (!AbstractEntity.class.isAssignableFrom(entityType)) {
	    throw new RuntimeException("Could not determined an editor for property " + getPropertyName() + " of type " + entityType + ".");
	}
	return ComponentFactory.createOnFocusLostAutocompleterWithEntityLocator(bindingEntity, bindingPropertyName, entityType, rootType, locatorManager, bindingEntity.getEntityFactory(), criteriaGenerator, getValueMatcher(), "key", "desc", caption, isSingle ? null : ",", toolTip, stringBinding);
    }

    public static class EntityLocatorValueMatcher<T extends AbstractEntity, R extends AbstractEntity> implements IValueMatcher<T>{

	private final IValueMatcher<T> autocompleterValueMatcher;

	private final ILocatorManager locatorManager;

	//private final Class<T> entityType;

	private final Class<R> rootType;

	private final String propertyName;

	private AbstractEntity<?> bindedEntity;

	public EntityLocatorValueMatcher(//
		final IValueMatcher<T> autocompleterValueMatcher,//
		final ILocatorManager locatorManager,//
		//	final Class<T> entityType,//
		final Class<R> rootType,//
		final String propertyName){
	    this.autocompleterValueMatcher = autocompleterValueMatcher;
	    this.locatorManager = locatorManager;
	    //  this.entityType = entityType;
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
	public fetch<?> getFetchModel() {
	    return autocompleterValueMatcher.getFetchModel();
	}

	@Override
	public void setFetchModel(final fetch<?> fetchModel) {
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

	private List<T> findMatches(final String value, final fetch<?> fetchModel) {
	    final ILocatorDomainTreeManager ldtm = ldtm();
	    if(ldtm.isUseForAutocompletion()){
		//TODO implement this method when the ldtm is used for autocompleter.
		return new ArrayList<T>();
	    }else{
		if(fetchModel == null){
		    return autocompleterValueMatcher.findMatches(value);
		} else {
		    return autocompleterValueMatcher.findMatchesWithModel(value);
		}
	    }

	}

	private ILocatorDomainTreeManager ldtm(){
	    ILocatorDomainTreeManager ldtm = locatorManager.getLocatorManager(rootType, propertyName);
	    if(ldtm == null){
		locatorManager.initLocatorManagerByDefault(rootType, propertyName);
		ldtm = locatorManager.getLocatorManager(rootType, propertyName);
		if(ldtm == null){
		    throw new IllegalStateException("The locator manager must be initialised");
		}
	    }
	    return ldtm;
	}

	@Override
	public Integer getPageSize() {
	    return autocompleterValueMatcher.getPageSize();
	}
    }

}
