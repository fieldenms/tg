package ua.com.fielden.platform.ui.config;

import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.entity.validation.annotation.NotNull;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController;

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
@CompanionObject(IEntityCentreConfigController.class)
@MapEntityTo("ENTITY_CENTRE_CONFIG")
public class EntityCentreConfig extends AbstractConfiguration<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @CompositeKeyMember(1)
    @Title(value = "User", desc = "Application user owning this configuration.")
    @MapTo("ID_CRAFT")
    private User owner;

    @IsProperty
    @CompositeKeyMember(2)
    @Title(value = "Title", desc = "Entity configuration title.")
    @MapTo("TITLE")
    private String title;

    @IsProperty
    @CompositeKeyMember(3)
    @Title(value = "Corresponding menu item", desc = "A property to specify a main menu item to which this configuration belongs")
    @MapTo("ID_MAIN_MENU")
    private MainMenuItem menuItem;

    @IsProperty
    @Title(value = "Is principal?", desc = "Indicates whether this configuration is the principal one and thus corresponds to a main menu item")
    @MapTo("IS_PRINCIPAL")
    private boolean principal = false;

    protected EntityCentreConfig() {
        setKey(new DynamicEntityKey(this));
    }

    public MainMenuItem getMenuItem() {
        return menuItem;
    }

    @Observable
    @EntityExists(MainMenuItem.class)
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
    @NotNull
    @EntityExists(User.class)
    public EntityCentreConfig setOwner(final User owner) {
        this.owner = owner;
        return this;
    }

    public String getTitle() {
        return title;
    }

    @Observable
    @NotNull
    public EntityCentreConfig setTitle(final String title) {
        this.title = title;
        return this;
    }

}
