package ua.com.fielden.platform.swing.model.callback;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.model.UmMasterWithCrudAndUpdater;

public class EditPostInitCallback<T extends AbstractEntity<?>, C extends IEntityDao<T>> implements IPostInitCallback<T, C> {

    @Override
    public <M extends UmMasterWithCrudAndUpdater<T, C>> void run(final M model) {
	model.getEditAction().actionPerformed(null);
    }

}
