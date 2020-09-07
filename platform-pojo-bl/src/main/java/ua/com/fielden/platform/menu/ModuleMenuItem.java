package ua.com.fielden.platform.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Represents the module's menu item with view.
 *
 * @author TG team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Title", desc = "Menu item title")
@CompanionObject(IModuleMenuItem.class)
@DescTitle(value = "Description", desc = "Menu item description")
public class ModuleMenuItem extends AbstractEntity<String> implements IMenuManager {
    private static final long serialVersionUID = 1L;

    @IsProperty(ModuleMenuItem.class)
    @Title("Submenu")
    private List<ModuleMenuItem> menu = new ArrayList<ModuleMenuItem>();

    @IsProperty
    @Title(value = "View", desc = "Menu item view")
    private View view;

    @IsProperty
    @Title(value = "Visible?", desc = "Is menu item visible?")
    private boolean visible = true;

    @IsProperty
    @Title(value = "Icon", desc = "Menu item icon")
    private String icon;

    @Observable
    public ModuleMenuItem setIcon(final String icon) {
        this.icon = icon;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    @Observable
    public ModuleMenuItem setVisible(final boolean isVisible) {
        this.visible = isVisible;
        return this;
    }

    public boolean isVisible() {
        return visible;
    }

    @Observable
    public ModuleMenuItem setView(final View view) {
        this.view = view;
        return this;
    }

    public View getView() {
        return view;
    }

    @Observable
    public ModuleMenuItem setMenu(final List<ModuleMenuItem> menu) {
        this.menu.clear();
        this.menu.addAll(menu);
        return this;
    }

    public List<ModuleMenuItem> getMenu() {
        return Collections.unmodifiableList(menu);
    }

    @Override
    public Optional<ModuleMenuItem> getMenuItem(final String title) {
        return menu.stream().filter(menuItem -> menuItem.getKey().equals(title)).findFirst();
    }

    @Override
    public boolean removeMenuItem(final String title) {
        return menu.removeIf(menuItem -> menuItem.getKey().equals(title));
    }

    @Override
    public void makeMenuItemInvisible(final String title) {
        menu.stream().filter(menuItem -> menuItem.getKey().equals(title)).findFirst().ifPresent(menuItem -> menuItem.setVisible(false));
    }

    @Override
    public ModuleMenuItem setKey(final String key) {
        return (ModuleMenuItem) super.setKey(key);
    }

    @Override
    public ModuleMenuItem setDesc(final String desc) {
        return (ModuleMenuItem) super.setDesc(desc);
    }
}