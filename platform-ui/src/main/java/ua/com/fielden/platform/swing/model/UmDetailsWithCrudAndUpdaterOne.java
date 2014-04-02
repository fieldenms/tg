package ua.com.fielden.platform.swing.model;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ua.com.fielden.platform.dao.IMasterDetailsDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.swing.ei.editors.development.ILightweightPropertyBinder;

/**
 * This class is a very simple addition to functionality already present in {@link UmDetailsWithCrudOne}. Specifically, it provide support for {@link FrameTitleUpdater} with
 * sensible frame title updates based on model action (new, save, edit etc.) events.
 * 
 * @author TG Team
 * 
 * @param <M>
 * @param <D>
 * @param <C>
 */
public abstract class UmDetailsWithCrudAndUpdaterOne<M extends AbstractEntity<?>, D extends AbstractEntity<?>, C extends IMasterDetailsDao<M, D>> extends UmDetailsWithCrudOne<M, D, C> {
    private final FrameTitleUpdater titleUpdater;

    protected UmDetailsWithCrudAndUpdaterOne(final M entity, final C companion, final ILightweightPropertyBinder<D> propertyBinder, final fetch<D> fm, final FrameTitleUpdater titleUpdater) {
        super(entity, companion, propertyBinder, fm);
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
            if (getEntity().isPersisted()) {
                titleUpdater.update(title + ": " + getEntity().getKey() + " -- " + getEntity().getDesc());
            }
            break;
        case EDIT_PRE_ACTION:
            titleUpdater.update(title + ": " + getEntity().getKey() + " -- " + getEntity().getDesc() + " (editing)");
            break;
        case NEW_POST_ACTION:
            titleUpdater.update(title + " (new)");
            break;
        case REFRESH_POST_ACTION:
            if (getEntity().isPersisted()) {
                titleUpdater.update(title + ": " + getEntity().getKey() + " -- " + getEntity().getDesc() + " (refreshed at " + now() + ")");
            }
            break;
        case SAVE_POST_ACTION_SUCCESSFUL:
            titleUpdater.update(title + ": " + getEntity().getKey() + " -- " + getEntity().getDesc() + " (saved at " + now() + ")");
            break;
        case SAVE_POST_ACTION_FAILED:
            titleUpdater.update(title + " (could not save)");
            break;
        }
    }

    protected FrameTitleUpdater getTitleUpdater() {
        return titleUpdater;
    }
}
