package ua.com.fielden.platform.menu;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.annotation.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/// Functional entity to save invisibility for main menu items represented by [WebMenuItemInvisibility].
///
@KeyType(String.class)
@KeyTitle("Save menu item key")
@CompanionObject(IMenuSaveAction.class)
public class MenuSaveAction extends AbstractFunctionalEntityWithCentreContext<String> {

    @IsProperty(String.class)
    @Title(value = "Visible menu items", desc = "Menu items that should become visible")
    private Set<String> visibleMenuItems = new HashSet<>();

    @IsProperty(String.class)
    @Title(value = "Invisible menu items", desc = "Menu items that should become invisible")
    private Set<String> invisibleMenuItems = new HashSet<>();

    @Observable
    public MenuSaveAction setInvisibleMenuItems(final Set<String> invisibleMenuItems) {
        this.invisibleMenuItems.clear();
        this.invisibleMenuItems.addAll(invisibleMenuItems);
        return this;
    }

    public Set<String> getInvisibleMenuItems() {
        return Collections.unmodifiableSet(invisibleMenuItems);
    }

    @Observable
    public MenuSaveAction setVisibleMenuItems(final Set<String> visibleMenuItems) {
        this.visibleMenuItems.clear();
        this.visibleMenuItems.addAll(visibleMenuItems);
        return this;
    }

    public Set<String> getVisibleMenuItems() {
        return Collections.unmodifiableSet(visibleMenuItems);
    }
}