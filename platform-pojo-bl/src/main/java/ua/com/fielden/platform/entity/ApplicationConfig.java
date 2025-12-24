package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.menu.Menu;

import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

/// An entity that aggregates application configuration options and is used to transport them to the client.
///
@KeyType(String.class)
@KeyTitle(value = "Title", desc = "Application title")
@CompanionObject(ApplicationConfigCo.class)
public class ApplicationConfig extends AbstractEntity<String> {

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

    @IsProperty
    @Title("Time Zone")
    private String timeZone;

    @IsProperty
    @Title(value = "Menu", desc = "Application menu configuration")
    private Menu menu;

    public Menu getMenu() {
        return menu;
    }

    @Observable
    public ApplicationConfig setMenu(final Menu menu) {
        this.menu = menu;
        return this;
    }

    public String getTimeZone() {
        return timeZone;
    }

    @Observable
    public ApplicationConfig setTimeZone(final String timeZone) {
        this.timeZone = timeZone;
        return this;
    }

    public Integer getDaysUntilSitePermissionExpires() {
        return daysUntilSitePermissionExpires;
    }

    @Observable
    public ApplicationConfig setDaysUntilSitePermissionExpires(final Integer daysUntilSitePermissionExpires) {
        this.daysUntilSitePermissionExpires = daysUntilSitePermissionExpires;
        return this;
    }

    @Observable
    protected ApplicationConfig setSiteAllowlist(final Set<String> siteAllowlist) {
        this.siteAllowlist.clear();
        this.siteAllowlist.addAll(siteAllowlist);
        return this;
    }

    public Set<String> getSiteAllowlist() {
        return unmodifiableSet(siteAllowlist);
    }

    @Observable
    public ApplicationConfig setCurrencySymbol(final String currencySymbol) {
        this.currencySymbol = currencySymbol;
        return this;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    @Observable
    public ApplicationConfig setUserName(final String userName) {
        this.userName = userName;
        return this;
    }

    public String getUserName() {
        return userName;
    }

}
