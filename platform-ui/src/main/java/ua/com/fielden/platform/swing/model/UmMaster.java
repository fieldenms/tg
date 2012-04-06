package ua.com.fielden.platform.swing.model;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.swing.ei.editors.development.ILightweightPropertyBinder;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;

/**
 * This is convenient UI model based on {@link UModel} simplifying model creation of a single stand alone (or simply master) entity. In this model the entity and managed entity is
 * the same thing.
 * <p>
 * For example, most of so called table codes represent stand alone entities.
 *
 * @author TG Team
 *
 * @param <T>
 *            -- entity type.
 * @param <C>
 *            -- controller type.
 */
public abstract class UmMaster<T extends AbstractEntity<?>, C> extends UModel<T, T, C> {
    /** Represents an entity model used for entity fetching and initialisation. */
    private final fetch<T> fm;

    protected UmMaster(final T entity, final C controller, final ILightweightPropertyBinder<T> propertyBinder, final fetch<T> fm, final boolean lazy) {
	super(entity, controller, propertyBinder, lazy);
	this.fm = fm != null ? fm : fetchAll((Class<T>)entity.getType());
	if (shouldEnforceEntityLoadingDuringInstantiation()) {
	    setEntity(findById(entity.getId(), true));
	}
    }

    /**
     * A method used inside the constructor to determine whether entity retrieval should be enforced during model instantiation. By default it return true, which is correct in most
     * cases. If the default is not suitable the derived model should override it to return false.
     *
     * @return
     */
    protected boolean shouldEnforceEntityLoadingDuringInstantiation() {
	return true;
    }

    /**
     * Should be implemented to correctly retrieve managed entity according the master model requirement as supposed to whatever was passed into the constructor.
     *
     * @param id
     * @param forceRetrieval
     * @return
     */
    protected abstract T findById(final Long id, final boolean forceRetrieval);

    @Override
    protected final T getManagedEntity() {
	return getEntity();
    }

    protected fetch<T> getFetchModel() {
	return fm;
    }

}