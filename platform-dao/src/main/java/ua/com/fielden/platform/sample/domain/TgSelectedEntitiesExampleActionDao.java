package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.annotation.EntityType;

/** 
 * DAO implementation for companion object {@link ITgSelectedEntitiesExampleAction}.
 * 
 * @author Developers
 *
 */
@EntityType(TgSelectedEntitiesExampleAction.class)
public class TgSelectedEntitiesExampleActionDao extends CommonEntityDao<TgSelectedEntitiesExampleAction> implements ITgSelectedEntitiesExampleAction {

    @Inject
    public TgSelectedEntitiesExampleActionDao(final IFilter filter) {
        super(filter);
    }

}