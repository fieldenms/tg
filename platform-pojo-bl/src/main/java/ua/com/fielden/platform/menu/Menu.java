package ua.com.fielden.platform.menu;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Represents device-profile-specific application menu with tiles and actions on them.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@CompanionObject(IMenu.class)
public class Menu extends AbstractEntity<String> implements IMenuManager {

    @IsProperty(Module.class)
    @Title("Menu")
    private List<Module> menu = new ArrayList<>();

    @IsProperty
    @Title("Edit menu items")
    private boolean canEdit;

    @IsProperty
    @Title("Desktop layout")
    private String whenDesktop;

    @IsProperty
    @Title("Tablet layout")
    private String whenTablet;

    @IsProperty
    @Title("Mobile layout")
    private String whenMobile;

    @IsProperty
    @Title("Min cell width")
    private String minCellWidth;

    @IsProperty
    @Title("Min cell height")
    private String minCellHeight;

    @IsProperty
    @Title("User name")
    private String userName;

    @IsProperty(Action.class)
    @Title("Actions")
    private List<Action> actions = new ArrayList<>();

    @Observable
    public Menu setActions(final List<Action> actions) {
        this.actions.clear();
        this.actions.addAll(actions);
        return this;
    }

    public List<Action> getActions() {
        return unmodifiableList(actions);
    }

    @Observable
    public Menu setUserName(final String userName) {
        this.userName = userName;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    @Observable
    public Menu setMinCellHeight(final String minCellHeight) {
        this.minCellHeight = minCellHeight;
        return this;
    }

    public String getMinCellHeight() {
        return minCellHeight;
    }

    @Observable
    public Menu setMinCellWidth(final String minCellWidth) {
        this.minCellWidth = minCellWidth;
        return this;
    }

    public String getMinCellWidth() {
        return minCellWidth;
    }

    @Observable
    public Menu setWhenMobile(final String whenMobile) {
        this.whenMobile = whenMobile;
        return this;
    }

    public String getWhenMobile() {
        return whenMobile;
    }

    @Observable
    public Menu setWhenTablet(final String whenTablet) {
        this.whenTablet = whenTablet;
        return this;
    }

    public String getWhenTablet() {
        return whenTablet;
    }

    @Observable
    public Menu setWhenDesktop(final String whenDesktop) {
        this.whenDesktop = whenDesktop;
        return this;
    }

    public String getWhenDesktop() {
        return whenDesktop;
    }

    @Observable
    public Menu setCanEdit(final boolean canEdit) {
        this.canEdit = canEdit;
        return this;
    }

    public boolean getCanEdit() {
        return canEdit;
    }

    @Observable
    public Menu setMenu(final List<Module> menu) {
        this.menu.clear();
        this.menu.addAll(menu);
        return this;
    }

    public List<Module> getMenu() {
        return unmodifiableList(menu);
    }

    @Override
    public Optional<Module> getMenuItem(final String title) {
        return menu.stream().filter(menuItem -> menuItem.getKey().equals(title)).findFirst();
    }

    @Override
    public boolean removeMenuItem(final String title) {
        return menu.removeIf(menuItem -> menuItem.getKey().equals(title));
    }

    @Override
    public void makeMenuItemInvisible(final String title) {
    }

}