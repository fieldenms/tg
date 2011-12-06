package ua.com.fielden.platform.swing.model;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.swing.ei.PropertyBinderWithDynamicAutocompleter;
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
public abstract class UmMasterWithCrudAndUpdater<T extends AbstractEntity, C extends IEntityDao<T>> extends UmMasterWithCrud<T, C> {
    private final FrameTitleUpdater titleUpdater;
    private final IUmViewOwner owner;

    protected UmMasterWithCrudAndUpdater(final IEntityProducer<T> entityProducer, final IEntityMasterCache cache, final T entity, final C controller, final PropertyBinderWithDynamicAutocompleter<T> propertyBinder, final fetch<T> fm, final FrameTitleUpdater titleUpdater, final IUmViewOwner owner, final boolean lazy) {
	super(entityProducer, cache, entity, controller, propertyBinder, fm, lazy);
	this.titleUpdater = titleUpdater;
	this.owner = owner;
    }

    protected UmMasterWithCrudAndUpdater(final IEntityProducer<T> entityProducer, final IEntityMasterCache cache, final T entity, final C controller, final PropertyBinderWithDynamicAutocompleter<T> propertyBinder, final fetch<T> fm, final FrameTitleUpdater titleUpdater, final boolean lazy) {
	this(entityProducer, cache, entity, controller, propertyBinder, fm, titleUpdater, null, lazy);
    }

    protected String now() {
	return DateTimeFormat.forPattern("HH:mm:ss").print(new DateTime());
    }

    @Override
    public void setEntity(final T entity) {
	super.setEntity(entity);
	if (owner != null) {
	    owner.notifyEntityChange(entity);
	}
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
