package ua.com.fielden.platform.menu;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
/**
 * Master entity object.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Save menu item key")
@CompanionObject(IMenuSaveAction.class)
public class MenuSaveAction extends AbstractFunctionalEntityWithCentreContext<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty(String.class)
    @Title(value = "Visible menu items", desc = "Menu items that should become visible")
    private Set<String> visibleMenuItems = new HashSet<String>();

    @IsProperty(String.class)
    @Title(value = "Invisible menu items", desc = "Menu items that should become invisible")
    private Set<String> invisibleMenuItems = new HashSet<String>();

    @Observable
    protected MenuSaveAction setInvisibleMenuItems(final Set<String> invisibleMenuItems) {
        this.invisibleMenuItems.clear();
        this.invisibleMenuItems.addAll(invisibleMenuItems);
        return this;
    }

    public Set<String> getInvisibleMenuItems() {
        return Collections.unmodifiableSet(invisibleMenuItems);
    }

    @Observable
    protected MenuSaveAction setVisibleMenuItems(final Set<String> visibleMenuItems) {
        this.visibleMenuItems.clear();
        this.visibleMenuItems.addAll(visibleMenuItems);
        return this;
    }

    public Set<String> getVisibleMenuItems() {
        return Collections.unmodifiableSet(visibleMenuItems);
    }
}