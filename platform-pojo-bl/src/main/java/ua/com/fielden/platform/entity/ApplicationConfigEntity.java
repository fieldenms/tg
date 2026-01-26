package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.*;

import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

/// An entity that aggregates application configuration options and is used to transport them to the client.
///
@KeyType(String.class)
@KeyTitle(value = "Title", desc = "Application title")
@CompanionObject(ApplicationConfigEntityCo.class)
public class ApplicationConfigEntity extends AbstractEntity<String> {

    @IsProperty
    @Title("User name")
    private String userName;

    @IsProperty
    @Title("Currency")
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
    @Title("Minimal Desktop Width")
    private int minDesktopWidth;

    @IsProperty
    @Title("Minimal Tablet Width")
    private int minTabletWidth;

    @IsProperty
    @Title("Locale")
    private String locale;

    @IsProperty
    @Title("Date Format")
    private String dateFormat;

    @IsProperty
    @Title("Time Format")
    private String timeFormat;

    @IsProperty
    @Title("Time with Milliseconds Format")
    private String timeWithMillisFormat;

    @IsProperty
    @Title("Master Action Options")
    private String masterActionOptions;

    @IsProperty
    @Title("First Day of the Week")
    private int firstDayOfWeek;

    public int getFirstDayOfWeek() {
        return firstDayOfWeek;
    }

    @Observable
    public ApplicationConfigEntity setFirstDayOfWeek(final int firstDayOfWeek) {
        this.firstDayOfWeek = firstDayOfWeek;
        return this;
    }
    public String getMasterActionOptions() {
        return masterActionOptions;
    }

    @Observable
    public ApplicationConfigEntity setMasterActionOptions(final String masterActionOptions) {
        this.masterActionOptions = masterActionOptions;
        return this;
    }
    public String getTimeWithMillisFormat() {
        return timeWithMillisFormat;
    }

    @Observable
    public ApplicationConfigEntity setTimeWithMillisFormat(final String timeWithMillisFormat) {
        this.timeWithMillisFormat = timeWithMillisFormat;
        return this;
    }
    public String getTimeFormat() {
        return timeFormat;
    }

    @Observable
    public ApplicationConfigEntity setTimeFormat(final String timeFormat) {
        this.timeFormat = timeFormat;
        return this;
    }
    public String getDateFormat() {
        return dateFormat;
    }

    @Observable
    public ApplicationConfigEntity setDateFormat(final String dateFormat) {
        this.dateFormat = dateFormat;
        return this;
    }
    public String getLocale() {
        return locale;
    }

    @Observable
    public ApplicationConfigEntity setLocale(final String locale) {
        this.locale = locale;
        return this;
    }
    public int getMinTabletWidth() {
        return minTabletWidth;
    }

    @Observable
    public ApplicationConfigEntity setMinTabletWidth(final int minTabletWidth) {
        this.minTabletWidth = minTabletWidth;
        return this;
    }
    public int getMinDesktopWidth() {
        return minDesktopWidth;
    }

    @Observable
    public ApplicationConfigEntity setMinDesktopWidth(final int minDesktopWidth) {
        this.minDesktopWidth = minDesktopWidth;
        return this;
    }

    public String getTimeZone() {
        return timeZone;
    }

    @Observable
    public ApplicationConfigEntity setTimeZone(final String timeZone) {
        this.timeZone = timeZone;
        return this;
    }

    public Integer getDaysUntilSitePermissionExpires() {
        return daysUntilSitePermissionExpires;
    }

    @Observable
    public ApplicationConfigEntity setDaysUntilSitePermissionExpires(final Integer daysUntilSitePermissionExpires) {
        this.daysUntilSitePermissionExpires = daysUntilSitePermissionExpires;
        return this;
    }

    @Observable
    protected ApplicationConfigEntity setSiteAllowlist(final Set<String> siteAllowlist) {
        this.siteAllowlist.clear();
        this.siteAllowlist.addAll(siteAllowlist);
        return this;
    }

    public Set<String> getSiteAllowlist() {
        return unmodifiableSet(siteAllowlist);
    }

    @Observable
    public ApplicationConfigEntity setCurrencySymbol(final String currencySymbol) {
        this.currencySymbol = currencySymbol;
        return this;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    @Observable
    public ApplicationConfigEntity setUserName(final String userName) {
        this.userName = userName;
        return this;
    }

    public String getUserName() {
        return userName;
    }

}
