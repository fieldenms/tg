package ua.com.fielden.platform.swing.model;

import java.nio.channels.SeekableByteChannel;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.swing.ei.development.MasterPropertyBinder;
import ua.com.fielden.platform.swing.view.IEntityMasterCache;

/**
 * This class is a very simple addition to functionality already present in {@link UmMasterWithCrud}. Specifically, it provide support for {@link FrameTitleUpdater} with sensible
 * frame title updates based on model action (new, save, edit etc.) events. Also, optionally, it supports {@link IUmViewOwner}.
 *
 * @author TG Team
 *
 * @param <T>
 * @param <C>
 */
public abstract class UmMasterWithCrudAndUpdater<T extends AbstractEntity<?>, C extends IEntityDao<T>> extends UmMasterWithCrud<T, C> {
    private final FrameTitleUpdater titleUpdater;
    private final IUmViewOwner owner;

    protected UmMasterWithCrudAndUpdater(final IEntityProducer<T> entityProducer, final IEntityMasterCache cache, final T entity, final C companion, final MasterPropertyBinder<T> propertyBinder, final fetch<T> fm, final FrameTitleUpdater titleUpdater, final IUmViewOwner owner, final boolean lazy) {
	super(entityProducer, cache, entity, companion, propertyBinder, fm, lazy);
	this.titleUpdater = titleUpdater;
	this.owner = owner;
    }

    protected UmMasterWithCrudAndUpdater(final IEntityProducer<T> entityProducer, final IEntityMasterCache cache, final T entity, final C companion, final MasterPropertyBinder<T> propertyBinder, final fetch<T> fm, final FrameTitleUpdater titleUpdater, final boolean lazy) {
	this(entityProducer, cache, entity, companion, propertyBinder, fm, titleUpdater, null, lazy);
    }

    protected String now() {
	return DateTimeFormat.forPattern("HH:mm:ss").print(new DateTime());
    }

    /**
     * Sets the specified entity instance and performs the owner notifications if it was provided.
     * In cases this is not desired this method should be overridden.
     * See {@link #setEntityWithoutNotification(AbstractEntity)} for more details.
     */
    @Override
    public void setEntity(final T entity) {
	super.setEntity(entity);
	if (owner != null) {
	    owner.notifyEntityChange(entity);
	}
    }

    /**
     * Method {@link #setEntity(AbstractEntity)} notifies view owner.
     * However, a more fine-grain control might be desired.
     * For example, if the owner should be notified only upon a successful save action then notification at any other time is inappropriate.
     * In order to implement such support, method {@link #setEntity(AbstractEntity)} needs to be overridden and a call to this method is made.
     * Additionally, method {@link #notifyActionStageChange(ua.com.fielden.platform.swing.model.UModel.ActionStage)} needs to be also overridden and when
     * a necessary event is received call <code>owner.notifyEntityChange(getEntity())</code>.
     * <p>
     * Of course, any other customisation of the notification logic is allowed.
     *
     * @param entity
     */
    protected void setEntityWithoutNotification(final T entity) {
	super.setEntity(entity);
    }

    protected abstract String defaultTitle();

    @Override
    protected void notifyActionStageChange(final ActionStage actionState) {
	final String title = defaultTitle();
	switch (actionState) {
	case CANCEL_POST_ACTION:
	    if (getManagedEntity().isPersisted()) {
		updateTitle(title + ": " + getManagedEntity().getKey() + " -- " + getManagedEntity().getDesc());
	    }
	    break;
	case EDIT_PRE_ACTION:
	    updateTitle(title + ": " + getManagedEntity().getKey() + " -- " + getManagedEntity().getDesc() + " (editing)");
	    break;
	case NEW_POST_ACTION: // falls through to EDIT_POST_ACTION to focus the preferred property
	    updateTitle(title + " (new)");
	case EDIT_POST_ACTION:
	    if (!StringUtils.isEmpty(getEntity().getPreferredProperty())) {
		getEditors().get(getEntity().getPreferredProperty()).getEditor().requestFocusInWindow();
	    }
	    break;
	case REFRESH_POST_ACTION:
	    if (getManagedEntity().isPersisted()) {
		updateTitle(title + ": " + getManagedEntity().getKey() + " -- " + getManagedEntity().getDesc() + " (refreshed at " + now() + ")");
	    }
	    break;
	case SAVE_POST_ACTION_SUCCESSFUL:
	    updateTitle(title + ": " + getManagedEntity().getKey() + " -- " + getManagedEntity().getDesc() + " (saved at " + now() + ")");
	    break;
	case SAVE_POST_ACTION_FAILED:
	    updateTitle(title + " (could not save)");
	    break;
	}
    }

    public FrameTitleUpdater getTitleUpdater() {
	return titleUpdater;
    }

    protected void updateTitle(final String message) {
	if (titleUpdater != null) {
	    titleUpdater.update(message);
	}
    }

    public IUmViewOwner getOwner() {
	return owner;
    }
}
