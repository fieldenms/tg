package ua.com.fielden.platform.swing.review;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.dao.IDaoFactory;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.swing.ei.editors.IPropertyEditor;
import ua.com.fielden.platform.swing.locator.ILocatorConfigurationRetriever;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.ui.config.api.interaction.ILocatorConfigurationController;

/**
 * This is a binder, which should be used whenever support for criteria parameters with dynamic autocompleter is required as part of the custom entity review.
 * 
 * @author TG Team
 * 
 * @param <T>
 */
public class CritPropertyBinderWithDynamicAutocompleter<T extends EntityQueryCriteria> extends CriteriaPropertyBinder<T> {

    private final IDaoFactory daoFactory;
    private final ILocatorConfigurationController locatorController;
    private final ILocatorConfigurationRetriever locatorRetriever;
    private final IEntityMasterManager entityMasterFactory;
    private final IValueMatcherFactory valueMatcherFactory;
    private final Class<T> criteriaType;

    public CritPropertyBinderWithDynamicAutocompleter(final Class<T> criteriaType, final IValueMatcherFactory valueMatcherFactory, final IEntityMasterManager entityMasterFactory, final IDaoFactory daoFactory, final ILocatorConfigurationController locatorController, final ILocatorConfigurationRetriever locatorRetriever) {

	this.criteriaType = criteriaType;
	this.daoFactory = daoFactory;
	this.entityMasterFactory = entityMasterFactory;
	this.valueMatcherFactory = valueMatcherFactory;

	this.locatorController = locatorController;
	this.locatorRetriever = locatorRetriever;
    }

    @Override
    protected IPropertyEditor createAutocompleter(final T entity, final MetaProperty metaProp, final IsProperty propertyAnnotation, final EntityType typeAnnotation, final boolean isSingle) {
	final Class elementType = propertyAnnotation.value() == PropertyDescriptor.class ? PropertyDescriptor.class : typeAnnotation.value();
	final String label = metaProp.getTitle() != null ? metaProp.getTitle().substring(0, 1).toUpperCase() + metaProp.getTitle().substring(1) : "title not set";
	final String tooltip = metaProp.getDesc();
	final String autocompleterCaption = metaProp.getDesc();// "filter by " + metaProp.getTitle() + "...";
	final IValueMatcher valueMatcher = entity.getValueMatcher(metaProp.getName());
	final IPropertyEditor editor = new OptionCollectionalPropertyEditor(entity, valueMatcherFactory, daoFactory, metaProp.getName(), elementType,//
	autocompleterCaption,//
	label, //
	tooltip,//
	valueMatcher,//
	entityMasterFactory, //
	locatorController, locatorRetriever, isSingle);
	return editor;
    }

    //    public String getPathForProperty(final String propertyName) {
    //	return pathToStoreReports + criteriaType.getSimpleName() + "_autocompleters" + System.getProperty("file.separator") + propertyName + ".dcf";
    //    }
}
