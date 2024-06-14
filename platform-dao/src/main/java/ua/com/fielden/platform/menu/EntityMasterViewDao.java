package ua.com.fielden.platform.menu;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.annotation.EntityType;
/** 
 * DAO implementation for companion object {@link IEntityMasterView}.
 * 
 * @author Developers
 *
 */
@EntityType(EntityMasterView.class)
public class EntityMasterViewDao extends CommonEntityDao<EntityMasterView> implements IEntityMasterView {

    @Inject
    public EntityMasterViewDao(final IFilter filter) {
        super(filter);
    }

}