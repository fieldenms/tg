package ua.com.fielden.platform.swing.review;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.ei.editors.EntityPropertyEditorWithDynamicAutocompleter;
import ua.com.fielden.platform.swing.ei.editors.IPropertyEditor;
import ua.com.fielden.platform.ui.config.api.interaction.ILocatorConfigurationController;

import com.google.inject.Inject;

public class DynamicReviewPropertyBinder<T extends AbstractEntity, DAO extends IEntityDao<T>> extends DynamicLocatorPropertyBinder<T, DAO> {

    private final ILocatorConfigurationController locatorController;
    private final LocatorPersistentObject locatorPersistentObject;

    @Inject
    public DynamicReviewPropertyBinder(final IEntityMasterManager entityMasterFactory, final ILocatorConfigurationController locatorController, final LocatorPersistentObject locatorPersistentObject) {
	super(entityMasterFactory);
	this.locatorController = locatorController;
	this.locatorPersistentObject = locatorPersistentObject;
    }

    @Override
    protected IPropertyEditor createAutocompleter(final DynamicEntityQueryCriteria entity, final String property) {
	final DynamicProperty editableProperty = entity.getEditableProperty(property);
	if (editableProperty.isEntityProperty() && editableProperty.isSingle()) {
	    return new EntityPropertyEditorWithDynamicAutocompleter(entity, property, getEntityMasterFactory(), locatorController, locatorPersistentObject);
	}
	return new OptionCollectionalPropertyEditor(entity, property, getEntityMasterFactory(), locatorController, locatorPersistentObject);
    }

    public ILocatorConfigurationController getLocatorController() {
	return locatorController;
    }

    public LocatorPersistentObject getLocatorPersistentObject() {
	return locatorPersistentObject;
    }
}
