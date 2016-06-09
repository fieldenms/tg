package ua.com.fielden.platform.web.test.server.master_action;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import java.util.Map;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import com.google.inject.Inject;

/** 
 * DAO implementation for companion object {@link INewEntityAction}.
 * 
 * @author Developers
 *
 */
@EntityType(NewEntityAction.class)
public class NewEntityActionDao extends CommonEntityDao<NewEntityAction> implements INewEntityAction {
    @Inject
    public NewEntityActionDao(final IFilter filter) {
        super(filter);
    }

}