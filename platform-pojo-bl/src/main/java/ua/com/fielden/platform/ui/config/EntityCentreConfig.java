package ua.com.fielden.platform.ui.config;

import ua.com.fielden.platform.dashboard.DashboardRefreshFrequency;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;
import ua.com.fielden.platform.entity.validation.RestrictCommasValidator;
import ua.com.fielden.platform.entity.validation.RestrictExtraWhitespaceValidator;
import ua.com.fielden.platform.entity.validation.RestrictNonPrintableCharactersValidator;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.ui.config.definers.EntityCentreConfigDashboardableDefiner;

import java.util.Date;

/**
 * A type designed for storing entity centres in a binary format, which can be used for storing configurations in databases, files etc.
 * <p>
 * This type introduces a dependency on a main menu item ({@link MainMenuItem}) to which a particular configuration belongs.<br>
 * Also, it brings in the property {@link #principal} for indicating whether a configuration instance is the main for a corresponding menu item.<br>
 * The principal configurations should be used when user activates a main menu item. <br>
 * Non-principal configuration could only be the result of user's <code>save as</code> action. Every non-principal configuration has a reference to a main menu item -- the one that
 * serves as a parent.
 * <p>
 * Deletion of the principal configurations is not required -- base user will always be able to change it.<br>
 * At the same time a corresponding menu item can always be made invisible by a base user, which should result in the invisibility of the whole menu branch including dynamic menu
 * items corresponding to the <code>save as</code> configurations of centres.
 * <p>
 * About uniqueness: each configuration belongs to an <code>owner</code> (i.e. user), has a <code>title</code> and corresponds to a <code>menuItem</code>. All three properties
 * together provide entity centre configuration uniqueness.<br>
 * However, due to the fact that non-base users use their and inherited configurations there can be a situation where there are two configurations with the same title under the
 * same menu -- one belonging to a base user, another -- to a derive user.<br>
 *
 * This situation can either be restricted at the application level upon saving of a configuration instance, or allowed, which could potentially lead to a confusion by user seeing
 * two menu items with the same title.
 *
 * @author TG Team
 */
@KeyType(DynamicEntityKey.class)
@KeyTitle("Configuration key")
@CompanionObject(EntityCentreConfigCo.class)
@MapEntityTo("ENTITY_CENTRE_CONFIG")
@DescTitle("Description")
@DenyIntrospection
public class EntityCentreConfig extends AbstractConfiguration<DynamicEntityKey> {

    @IsProperty
    @CompositeKeyMember(1)
    @Title(value = "User", desc = "Application user owning this configuration.")
    @MapTo("ID_CRAFT")
    private User owner;

    @IsProperty
    @CompositeKeyMember(2)
    @Title(value = "Title", desc = "Entity configuration title.")
    @MapTo("TITLE")
    @SkipDefaultStringKeyMemberValidation({RestrictNonPrintableCharactersValidator.class, RestrictExtraWhitespaceValidator.class, RestrictCommasValidator.class})
    private String title;

    @IsProperty
    @CompositeKeyMember(3)
    @Title(value = "Corresponding menu item", desc = "A property to specify a main menu item to which this configuration belongs.")
    @MapTo("ID_MAIN_MENU")
    private MainMenuItem menuItem;

    @IsProperty
    @Title(value = "Is principal?", desc = "Indicates whether this configuration is the principal one and thus corresponds to a main menu item.")
    @MapTo("IS_PRINCIPAL")
    private boolean principal = false;

    @IsProperty
    @Title(value = "Is preferred?", desc = "Indicates whether this configuration is preferred over the others on the same menu item.")
    @MapTo
    private boolean preferred = false;

    @IsProperty
    @MapTo
    @Title(value = "Config UUID", desc = "UUID of centre configuration [represented by this EntityCentreConfig instance] for the user that created it (SAVED or FRESH surrogate kind) or other users with which it was shared / based-on (FRESH surrogate kind only).")
    private String configUuid;

    @IsProperty
    @MapTo
    @Title(value = "Dashboardable?", desc = "Indicates whether this configuration is dashboardable i.e. it is present in owner's dashboard or dashboards of users with which it was shared / based-on.")
    @AfterChange(EntityCentreConfigDashboardableDefiner.class)
    private boolean dashboardable = false;

    @IsProperty
    @MapTo
    @Title(value = "Dashboardable Date", desc = "Date when this configuration was made dashboardable.")
    private Date dashboardableDate;

    @IsProperty
    @MapTo
    @Title(value = "Dashboard Refresh Frequency", desc = "Defines how frequently this configuration should be refreshed as part of the dashboard refresh lifecycle.")
    private DashboardRefreshFrequency dashboardRefreshFrequency;

    @IsProperty
    @MapTo
    @Title(value = "Run Automatically?", desc = "Defines whether this configuration should be auto run upon loading.")
    private boolean runAutomatically = false;

    @Observable
    public EntityCentreConfig setRunAutomatically(final boolean runAutomatically) {
        this.runAutomatically = runAutomatically;
        return this;
    }

    public boolean isRunAutomatically() {
        return runAutomatically;
    }

    @Observable
    public EntityCentreConfig setDashboardRefreshFrequency(final DashboardRefreshFrequency dashboardRefreshFrequency) {
        this.dashboardRefreshFrequency = dashboardRefreshFrequency;
        return this;
    }

    public DashboardRefreshFrequency getDashboardRefreshFrequency() {
        return dashboardRefreshFrequency;
    }

    @Observable
    public EntityCentreConfig setDashboardableDate(final Date dashboardableDate) {
        this.dashboardableDate = dashboardableDate;
        return this;
    }

    public Date getDashboardableDate() {
        return dashboardableDate;
    }

    @Observable
    public EntityCentreConfig setDashboardable(final boolean dashboardable) {
        this.dashboardable = dashboardable;
        return this;
    }

    public boolean isDashboardable() {
        return dashboardable;
    }

    @Observable
    public EntityCentreConfig setConfigUuid(final String configUuid) {
        this.configUuid = configUuid;
        return this;
    }

    public String getConfigUuid() {
        return configUuid;
    }

    public boolean isPreferred() {
        return preferred;
    }

    @Observable
    public EntityCentreConfig setPreferred(final boolean value) {
        preferred = value;
        return this;
    }

    public MainMenuItem getMenuItem() {
        return menuItem;
    }

    @Observable
    public EntityCentreConfig setMenuItem(final MainMenuItem menuItem) {
        this.menuItem = menuItem;
        return this;
    }

    public boolean isPrincipal() {
        return principal;
    }

    @Observable
    public EntityCentreConfig setPrincipal(final boolean flag) {
        this.principal = flag;
        return this;
    }

    public User getOwner() {
        return owner;
    }

    @Observable
    public EntityCentreConfig setOwner(final User owner) {
        this.owner = owner;
        return this;
    }

    public String getTitle() {
        return title;
    }

    @Observable
    public EntityCentreConfig setTitle(final String title) {
        this.title = title;
        return this;
    }

    @Override
    @Observable
    public EntityCentreConfig setConfigBody(final byte[] configBody) {
        return (EntityCentreConfig) super.setConfigBody(configBody);
    }

    @Override
    @Observable
    public EntityCentreConfig setDesc(String desc) {
        return super.setDesc(desc);
    }

}
