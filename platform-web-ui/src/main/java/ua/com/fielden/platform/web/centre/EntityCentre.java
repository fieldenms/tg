package ua.com.fielden.platform.web.centre;

import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;

/**
 * Represents the entity centre.
 *
 * @author TG Team
 *
 */
public class EntityCentre {

    private final Class<? extends MiWithConfigurationSupport<?>> menuItemType;
    private final String name;
    private final EntityCentreConfig dslDefaultConfig;

    /**
     * Creates new {@link EntityCentre} instance for the menu item type and with specified name.
     *
     * @param menuItemType
     *            - the menu item type for which this entity centre is to be created.
     * @param name
     *            - the name for this entity centre.
     * @param dslDefaultConfig
     *            -- default configuration taken from Centre DSL
     */
    public EntityCentre(final Class<? extends MiWithConfigurationSupport<?>> menuItemType, final String name, final EntityCentreConfig dslDefaultConfig) {
        this.menuItemType = menuItemType;
        this.name = name;
        this.dslDefaultConfig = dslDefaultConfig;
    }

    /**
     * Returns the menu item type for this {@link EntityCentre} instance.
     *
     * @return
     */
    public Class<? extends MiWithConfigurationSupport<?>> getMenuItemType() {
        return this.menuItemType;
    }

    /**
     * Returns the entity centre name.
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Default configuration taken from Centre DSL.
     *
     * @return
     */
    public EntityCentreConfig getDslDefaultConfig() {
        return dslDefaultConfig;
    }
}
