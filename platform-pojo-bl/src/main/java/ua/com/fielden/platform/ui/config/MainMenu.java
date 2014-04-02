package ua.com.fielden.platform.ui.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.CompanionObject;

/**
 * An entity that holds all menu items.
 * 
 * @author TG Team
 * 
 */
@KeyType(String.class)
@KeyTitle(value = "Main Menu key", desc = "Main Menu key description")
@CompanionObject(IMainMenu.class)
public class MainMenu extends AbstractEntity<String> {
    @IsProperty(String.class)
    @Title(value = "Menu Items and pop markers", desc = "All menu items (and pop markers) in one bunch")
    private List<String> menuItems = new ArrayList<String>();

    @Observable
    public MainMenu setMenuItems(final List<String> menuItems) {
        this.menuItems.clear();
        this.menuItems.addAll(menuItems);
        return this;
    }

    public List<String> getMenuItems() {
        return Collections.unmodifiableList(menuItems);
    }
}