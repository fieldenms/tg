package ua.com.fielden.platform.swing.model.callback;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.model.UmMasterWithCrudAndUpdater;

/**
 * 
 * UI models support an even loop. There is a special event called INIT_POST_ACTION that indicates completion of model initialisation stage. Instances of this contract can be
 * provided in order to customize post initialisation behaviour such as putting UI model into an edit mode.
 * <p>
 * Please note that method {@link #run(UmMasterWithCrudAndUpdater)} is invoked on EDT, so no heavy logic should be executed in it directly.
 * 
 * @author TG Team
 * 
 * @param <T>
 * @param <C>
 */
public interface IPostInitCallback<T extends AbstractEntity<?>, C extends IEntityDao<T>> {
    <M extends UmMasterWithCrudAndUpdater<T, C>> void run(final M model);
}
