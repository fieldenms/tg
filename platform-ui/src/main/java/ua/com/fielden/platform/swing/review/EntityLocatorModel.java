package ua.com.fielden.platform.swing.review;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.Action;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder;

public class EntityLocatorModel<T extends AbstractEntity, DAO extends IEntityDao<T>, C extends EntityQueryCriteria<T, DAO>> extends EntityReviewModel<T, DAO, C> {

    private final IEntitySelectionListener entitySelectionListener;

    public EntityLocatorModel(//
    final C criteria,//
    final PropertyTableModelBuilder<T> builder,//
    final IEntitySelectionListener entitySelectionListener,//
    final Runnable... afterRunActions) {
	super(criteria, builder, null, afterRunActions);
	this.entitySelectionListener = entitySelectionListener;
    }

    public Action createSelectAction(final EntityLocator<T, DAO, C> entityLocator) {
	final Action action = new Command<Void>("Select") {

	    private static final long serialVersionUID = 553945456811710856L;

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		final List<T> selectedItems = entityLocator.getSelectedItems();
		for (final T selectedItem : selectedItems) {
		    entitySelectionListener.performSelection(selectedItem);
		}
		return null;
	    }

	};
	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
	// action.putValue(Action.SHORT_DESCRIPTION, "Selects");
	action.setEnabled(true);
	return action;
    }

    public IEntitySelectionListener getSelectionListener() {
	return entitySelectionListener;
    }

}
