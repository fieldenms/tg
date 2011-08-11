package ua.com.fielden.platform.swing.model;

import ua.com.fielden.platform.dao.IMasterDetailsDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.swing.ei.editors.ILightweightPropertyBinder;

/**
 * This is a UI model based on {@link UModel}, which enforces master/details relationship.
 *
 * @author TG Team
 *
 * @param <T>
 *            -- entity type.
 * @param <C>
 *            -- controller type.
 */
public abstract class UmDetails<M extends AbstractEntity, D extends AbstractEntity, C extends IMasterDetailsDao<M, D>> extends UModel<M, D, C> {
    /** Represents an entity model used for details entity fetching and initialisation. */
    private final fetch<D> fm;

    /**
     * Primary constructor.
     *
     * @param entity
     *            -- an instance of a master entity.
     * @param controller
     *            -- controller used for managing details entity instances.
     * @param propertyBinder
     *            -- builder for instantiation of editors for editing details entity instance.
     * @param fm
     *            -- query model used during retrieval of details entities, which identifies how much they should be initialised.
     * @param lazy
     *            -- identifies whether this is a lazy model.
     */
    protected UmDetails(final M entity, final C controller, final ILightweightPropertyBinder<D> propertyBinder, final fetch<D> fm, final boolean lazy) {
	super(entity, controller, propertyBinder, lazy);
	this.fm = fm;
    }

    protected fetch<D> getFetchModel() {
	return fm;
    }

    /**
     * Ensures that master entity is set and the model is reinitialised if required by triggering the refresh action.
     */
    @Override
    public void setEntity(final M entity) {
	final boolean shouldRefresh = shouldRefreshWhenSettingEntity(entity);
	super.setEntity(entity);
	if (shouldRefresh) {
	    getRefreshAction().actionPerformed(null);
	}
    };

    /**
     * Is invoked when {@link #setEntity(AbstractEntity)} is called in order to check if the refresh action should be performed.
     * <p>
     * Can be overridden to provide a custom refresh request logic.
     *
     * @param entity
     * @return
     */
    protected boolean shouldRefreshWhenSettingEntity(final M entity) {
	return (getEntity().getId() != entity.getId()) && isInitialised();
    }

    @Override
    public boolean canOpen() {
	return getEntity().isPersisted();
    }

    @Override
    public String whyCannotOpen() {
	return "No details can be added before the master entity is saved.";
    }

}
