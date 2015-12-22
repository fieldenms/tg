package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;

import java.util.Map;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import com.google.inject.Inject;

/** 
 * DAO implementation for companion object {@link ITgUpdateRolesAction}.
 * 
 * @author Developers
 *
 */
@EntityType(TgUpdateRolesAction.class)
public class TgUpdateRolesActionDao extends CommonEntityDao<TgUpdateRolesAction> implements ITgUpdateRolesAction {
    @Inject
    public TgUpdateRolesActionDao(final IFilter filter) {
        super(filter);
    }
    
    @Override
    @SessionRequired
    public TgUpdateRolesAction save(final TgUpdateRolesAction entity) {
        final Result res = entity.isValid();
        if (!res.isSuccessful()) {
            throw res;
        }
        
        
        // TODO Auto-generated method stub
        return super.save(entity);
    }

}