package ua.com.fielden.platform.menu;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.annotation.EntityType;
/** 
 * DAO implementation for companion object {@link IEntityCentreView}.
 * 
 * @author Developers
 *
 */
@EntityType(EntityCentreView.class)
public class EntityCentreViewDao extends CommonEntityDao<EntityCentreView> implements IEntityCentreView {

    @Inject
    public EntityCentreViewDao(final IFilter filter) {
        super(filter);
    }

}