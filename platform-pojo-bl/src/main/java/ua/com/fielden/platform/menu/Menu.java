package ua.com.fielden.platform.menu;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.annotation.Observable;

import java.util.*;
import java.util.Optional;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;

/**
 * Represents device-profile-specific application menu with tiles and actions on them.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@CompanionObject(MenuCo.class)
public class Menu extends AbstractEntity<String> implements IMenuManager {

    @IsProperty(ModuleMenu.class)
    @Title("Menu")
    private final List<ModuleMenu> menu = new ArrayList<>();

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

    @IsProperty
    private String currencySymbol;

    @IsProperty(String.class)
    @Title(value = "Site Allow List", desc = "Site white list that user can visit without confirmation.")
    private final Set<String> siteAllowlist = new HashSet<>();

    @IsProperty
    @Title(value = "Allowed Site Expiry (Days)", desc = "Defines how long an allowed site remains trusted before requiring re-confirmation.")
    private Integer daysUntilSitePermissionExpires;

    public Integer getDaysUntilSitePermissionExpires() {
        return daysUntilSitePermissionExpires;
    }

    @Observable
    public Menu setDaysUntilSitePermissionExpires(final Integer daysUntilSitePermissionExpires) {
        this.daysUntilSitePermissionExpires = daysUntilSitePermissionExpires;
        return this;
    }
    
    @Observable
    protected Menu setSiteAllowlist(final Set<String> siteAllowlist) {
        this.siteAllowlist.clear();
        this.siteAllowlist.addAll(siteAllowlist);
        return this;
    }

    public Set<String> getSiteAllowlist() {
        return unmodifiableSet(siteAllowlist);
    }

    @Observable
    public Menu setCurrencySymbol(final String currencySymbol) {
        this.currencySymbol = currencySymbol;
        return this;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
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
    public Menu setMenu(final List<ModuleMenu> menu) {
        this.menu.clear();
        this.menu.addAll(menu);
        return this;
    }

    @Override
    public List<ModuleMenu> getMenu() {
        return unmodifiableList(menu);
    }

    @Override
    public Optional<ModuleMenu> getMenuItem(final String title) {
        return menu.stream().filter(menuItem -> menuItem.getKey().equals(title)).findFirst();
    }

    @Override
    public boolean removeMenuItem(final String title) {
        return menu.removeIf(menuItem -> menuItem.getKey().equals(title));
    }

    @Override
    public void makeMenuItemInvisible(final String title) {
    }

    @Override
    public void makeMenuItemSemiVisible(final String title) {
    }

    @Override
    public String getTitle() {
        return getKey();
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public boolean isSemiVisible() {
        return false;
    }

}