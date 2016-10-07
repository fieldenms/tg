package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/** 
 * DAO implementation for companion object {@link IOrgUnit}.
 * 
 * @author Developers
 *
 */
@EntityType(TgOrgUnit.class)
public class TgOrgUnitDao extends CommonEntityDao<TgOrgUnit> implements ITgOrgUnit {
    
    @Inject
    public TgOrgUnitDao(final IFilter filter) {
        super(filter);
    }
    
//    @Override
//    @SessionRequired
//    public void delete(final OrgUnit entity) {
//        defaultDelete(entity);
//    }
//    
//    @Override
//    @SessionRequired
//    public void delete(final EntityResultQueryModel<OrgUnit> model, final Map<String, Object> paramValues) {
//        defaultDelete(model, paramValues);
//    }
    
}