package ua.com.fielden.platform.swing.model.callback;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.model.UmMasterWithCrudAndUpdater;
import ua.com.fielden.platform.swing.model.callback.IPostInitCallback;

public class NewEditPostinitCallback<T extends AbstractEntity<?>, C extends IEntityDao<T>> implements IPostInitCallback<T, C> {

    @Override
    public <M extends UmMasterWithCrudAndUpdater<T, C>> void run(final M model) {
	if (model.getEntity().isPersisted()) {
	    model.getEditAction().actionPerformed(null);
	} else {
	    model.getNewAction().actionPerformed(null);
	}
    }

}
