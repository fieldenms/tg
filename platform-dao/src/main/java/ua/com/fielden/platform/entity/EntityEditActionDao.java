package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import java.util.Map;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import com.google.inject.Inject;

/** 
 * DAO implementation for companion object {@link IEntityEditAction}.
 * 
 * @author Developers
 *
 */
@EntityType(EntityEditAction.class)
public class EntityEditActionDao extends CommonEntityDao<EntityEditAction> implements IEntityEditAction {
    @Inject
    public EntityEditActionDao(final IFilter filter) {
        super(filter);
    }

}