package ua.com.fielden.platform.swing.model;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ua.com.fielden.platform.dao2.IMasterDetailsDao2;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fetch;
import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;
import ua.com.fielden.platform.swing.ei.editors.development.ILightweightPropertyBinder;

/**
 * This class is a very simple addition to functionality already present in {@link UmDetailsWithCrudMany}. Specifically, it provide support for {@link FrameTitleUpdater} with
 * sensible frame title updates based on model action (new, save, edit etc.) events.
 *
 * @author TG Team
 *
 * @param <M>
 * @param <D>
 * @param <C>
 */
public abstract class UmDetailsWithCrudAndUpdaterMany<M extends AbstractEntity<?>, D extends AbstractEntity<?>, C extends IMasterDetailsDao2<M, D>> extends UmDetailsWithCrudMany<M, D, C> {
    private final FrameTitleUpdater titleUpdater;

    protected UmDetailsWithCrudAndUpdaterMany(final M entity, final C controller, final ILightweightPropertyBinder<D> propertyBinder, final fetch<D> fm, final PropertyTableModel<D> tableModel, final FrameTitleUpdater titleUpdater, final boolean lazy) {
	super(entity, controller, propertyBinder, fm, tableModel, lazy);
	this.titleUpdater = titleUpdater;
    }

    private String now() {
	final DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm:ss");
	return fmt.print(new DateTime());
    }

    protected abstract String defaultTitle();

    @Override
    protected void notifyActionStageChange(final ActionStage actionState) {
	final String title = defaultTitle();
	switch (actionState) {
	case CANCEL_POST_ACTION:
	    if (getEntity().isPersisted() && titleUpdater != null) {
		titleUpdater.update(title + ": " + getEntity().getKey() + " -- " + getEntity().getDesc());
	    }
	    break;
	case EDIT_PRE_ACTION:
	    if (titleUpdater != null) {
		titleUpdater.update(title + ": " + getEntity().getKey() + " -- " + getEntity().getDesc() + " (editing)");
	    }
	    break;
	case NEW_POST_ACTION:
	    if (titleUpdater != null) {
		titleUpdater.update(title + " (new)");
	    }
	    break;
	case REFRESH_POST_ACTION:
	    if (getEntity().isPersisted() && titleUpdater != null) {
		titleUpdater.update(title + ": " + getEntity().getKey() + " -- " + getEntity().getDesc() + " (refreshed at " + now() + ")");
	    }
	    break;
	case SAVE_POST_ACTION_SUCCESSFUL:
	    if (titleUpdater != null) {
		titleUpdater.update(title + ": " + getEntity().getKey() + " -- " + getEntity().getDesc() + " (saved at " + now() + ")");
	    }
	    break;
	case SAVE_POST_ACTION_FAILED:
	    if (titleUpdater != null) {
		titleUpdater.update(title + " (could not save)");
	    }
	    break;
	}
    }
}
