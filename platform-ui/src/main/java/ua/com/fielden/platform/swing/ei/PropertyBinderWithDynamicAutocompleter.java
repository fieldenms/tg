package ua.com.fielden.platform.swing.ei;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.dao.IDaoFactory;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.swing.ei.editors.EntityPropertyEditorWithDynamicAutocompleter;
import ua.com.fielden.platform.swing.ei.editors.IPropertyEditor;
import ua.com.fielden.platform.swing.locator.ILocatorConfigurationRetriever;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.ui.config.api.interaction.ILocatorConfigurationController;

public class PropertyBinderWithDynamicAutocompleter<T extends AbstractEntity> extends LightweightPropertyBinder<T> {

    private final IDaoFactory daoFactory;
    private final ILocatorConfigurationController locatorController;
    private final ILocatorConfigurationRetriever locatorRetriever;

    public PropertyBinderWithDynamicAutocompleter(final IValueMatcherFactory valueMatcherFactory, final IEntityMasterManager entityMasterFactory, final IDaoFactory daoFactory, final ILocatorConfigurationController locatorController, final ILocatorConfigurationRetriever locatorRetriever, final String... propetyToIgnor) {
	super(valueMatcherFactory, entityMasterFactory, propetyToIgnor);
	this.daoFactory = daoFactory;
	this.locatorController = locatorController;
	this.locatorRetriever = locatorRetriever;
    }

    @Override
    protected IPropertyEditor createEntityPropertyEditor(final T entity, final String name, final IValueMatcher<?> valueMatcher) {
	return new EntityPropertyEditorWithDynamicAutocompleter(entity, name, valueMatcher, getEntityMasterFactory(), getValueMatcherFactory(), daoFactory, locatorController, locatorRetriever);
    }
}
