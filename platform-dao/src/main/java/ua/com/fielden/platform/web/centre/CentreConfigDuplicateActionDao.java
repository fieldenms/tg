package ua.com.fielden.platform.web.centre;

import java.util.HashMap;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/** 
 * DAO implementation for companion object {@link ICentreConfigDuplicateAction}.
 * 
 * @author TG Team
 *
 */
@EntityType(CentreConfigDuplicateAction.class)
public class CentreConfigDuplicateActionDao extends CommonEntityDao<CentreConfigDuplicateAction> implements ICentreConfigDuplicateAction {
    
    @Inject
    public CentreConfigDuplicateActionDao(final IFilter filter) {
        super(filter);
    }
    
    @Override
    @SessionRequired
    public CentreConfigDuplicateAction save(final CentreConfigDuplicateAction entity) {
        entity.setCustomObject(new HashMap<>()); // clear custom object not to bind centre information second time (it binds after retrieval for the first time, see client-side _bindCentreInfo method)
        return entity;
    }
    
}