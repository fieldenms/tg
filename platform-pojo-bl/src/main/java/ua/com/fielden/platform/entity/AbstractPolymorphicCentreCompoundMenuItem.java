package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

import static java.lang.String.format;

/// Functional entity for an entity master with an entity centre that can be changed by server side logic.
///
@EntityTitle("Compound Menu Item for Polymorphic Entity Centre")
public abstract class AbstractPolymorphicCentreCompoundMenuItem<T extends AbstractEntity<?>> extends AbstractFunctionalEntityForCompoundMenuItem<T> {

    @IsProperty
    @Title(value = "Menu Item Type", desc = "Entity Centre Menu Item Type")
    private String menuItemType;

    @IsProperty
    @Title("Import URI")
    private String importUri;

    @IsProperty
    @Title("Element Name")
    private String elementName;

    @IsProperty
    @Title(value = "Should Force Post Save Refresh?", desc="Indicates whether the centre should be refreshed after a successful save of the entity master")
    private boolean shouldEnforcePostSaveRefresh;

    @IsProperty
    @Title(value = "Event Source Class", desc = "Event Source Class Name")
    private String eventSourceClass;

    public String getEventSourceClass() {
        return eventSourceClass;
    }

    @Observable
    public AbstractPolymorphicCentreCompoundMenuItem<T> setEventSourceClass(final String eventSourceClass) {
        this.eventSourceClass = eventSourceClass;
        return this;
    }

    public boolean getShouldEnforcePostSaveRefresh() {
        return shouldEnforcePostSaveRefresh;
    }

    @Observable
    public AbstractPolymorphicCentreCompoundMenuItem<T> setShouldEnforcePostSaveRefresh(final boolean shouldEnforcePostSaveRefresh) {
        this.shouldEnforcePostSaveRefresh = shouldEnforcePostSaveRefresh;
        return this;
    }

    public String getMenuItemType() {
        return menuItemType;
    }

    @Observable
    public AbstractPolymorphicCentreCompoundMenuItem<T> setMenuItemType(final String menuItemType) {
        this.menuItemType = menuItemType;
        return this;
    }

    @Observable
    public AbstractPolymorphicCentreCompoundMenuItem<T> setElementName(final String elementName) {
        this.elementName = elementName;
        return this;
    }

    public String getElementName() {
        return elementName;
    }

    @Observable
    protected AbstractPolymorphicCentreCompoundMenuItem<T> setImportUri(final String importUri) {
        this.importUri = importUri;
        return this;
    }

    public String getImportUri() {
        return importUri;
    }

    public AbstractPolymorphicCentreCompoundMenuItem<T> setMenuItemTypeForCentre(final Class<? extends MiWithConfigurationSupport<?>> menuItemTypeAsClass) {
        setMenuItemType(menuItemTypeAsClass.getName());
        setImportUri(format("/centre_ui/%s", menuItemTypeAsClass.getName()));
        setElementName(format("tg-%s-centre", menuItemTypeAsClass.getSimpleName()));
        return this;
    }
}
