package ua.com.fielden.platform.web.centre.api.actions;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A configuration of an entity related action, which is associated with every entity that is represented by an entity centre.
 * <p>
 * These actions should be nameless as there would several instances of the same action as part of a single entity centre -- one for each retrieved entity.
 * However, it would still be potentially desirable to disable other entity actions if one of them has been activated and is still in progress.
 * For this, an entity specific and local to the entity centre message channel should be created with ability for all associated actions to publish
 * and subscribe to generic messages, designed specifically to coordinate their group state.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IEntityActionBuilder<T extends AbstractEntity<?>> {
    IEntityActionBuilder0<T> addAction(final Class<? extends AbstractEntity<?>> functionalEntity);

    /**
     * In case where no action should be associated with places that have default actions associated with them automatically.
     * For example, all properties in a result set have a default action <code>open master</code>.
     *
     * @return
     */
    IEntityActionBuilder7<T> noAction();

}
